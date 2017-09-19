/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byd5.ats.rabbitmq;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.service.RunTaskService;
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收并处理识别跟踪的到站消息和离站消息
 * 
 */
public class ReceiverTrace {
	
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverTrace.class);
		
	@Autowired
	private RunTaskService runTaskService;
	
	@Autowired
	private SenderDepart sender;
	@Autowired
	private TrainrungraphHystrixService trainrungraphHystrixService;
	
	/**
	 * 到站(不管是否停稳)消息处理：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@RabbitListener(queues = "#{queueTraceStationEnter.name}")
	public void receiveTraceStationEnter(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.enter] '" + in + "'");
		//System.out.println("[trace] '" + in + "'");
		//doWork(in);
		TrainEventPosition event = null;
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		event = objMapper.readValue(in, TrainEventPosition.class);
		
		// 检查该车是否有记录
		Integer carNum = (int) event.getCarNum();
		
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		String dsStationNum = event.getDstStationNum();
		
		//-------------获取或 更新运行图任务信息
		runTaskService.clearMapRuntask(carNum, tablenum);//非计划车时，移除残留的计划车运行任务信息
		getRuntask(carNum, tablenum, trainnum, dsStationNum);
		
		TrainRunTask task = runTaskService.getMapRuntask(carNum);
		
		//添加列车到站信息
		runTaskService.updateMapTrace(carNum, event);
		
		//---------------停站时间列表为空，则查询数据库获取--------------
		if(runTaskService.mapDwellTime.size() == 0){
			try{
				//String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
				String resultMsg = trainrungraphHystrixService.getDwellTime();
				if(resultMsg != null && !resultMsg.equals("error")){
					List<AppDataDwellTimeCommand> dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
					for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
						runTaskService.mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
					}
				}else if(resultMsg == null){
					LOG.error("[trace.station.enter] serv31-trainrungraph fallback getRuntaskAllCommand is null!");
				}
			}catch (Exception e) {
				// TODO: handle exception
				LOG.error("[trace.station.enter] getRuntaskAllCommand parse error!");
				//e.printStackTrace();
			}
		}
		
		
		// 向该车发送站间运行等级
		AppDataATOCommand appDataATOCommand = null;

		if (task != null) {//计划车
			appDataATOCommand = runTaskService.aodCmdEnter(task, event);
			LOG.info("[trace.station.enter] ATOCommand: next station ["
					+ appDataATOCommand.getNextStationId() + "] section run time ["
					+ appDataATOCommand.getSectionRunLevel()+ "s]"
					+ "section stop time ["+ appDataATOCommand.getStationStopTime()
					+ "s]");
			
			if(appDataATOCommand.getSkipNextStation() == 0x55){//若列车下一站有跳停，则连续给车发3次命令
				sender.sendATOCommand(appDataATOCommand);
				sender.sendATOCommand(appDataATOCommand);
			}
			sender.sendATOCommand(appDataATOCommand);
		}
		else {//非计划车到站时的处理
			LOG.info("[trace.station.enter] unplanTrain----");
			//appDataATOCommand = runTaskService.aodCmdEnterUnplan(event);
			LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}
		
		/*LOG.info("[trace.station.enter] ATOCommand: next station ["
				+ appDataATOCommand.getNextStationId() + "] section run time ["
				+ appDataATOCommand.getSectionRunLevel()+ "s]"
				+ "section stop time ["+ appDataATOCommand.getStationStopTime()
				+ "s]");
		
		
		if(appDataATOCommand.getSkipNextStation() == 0x55){//若列车下一站有跳停，则连续给车发3次命令
			sender.sendATOCommand(appDataATOCommand);
			sender.sendATOCommand(appDataATOCommand);
		}
		sender.sendATOCommand(appDataATOCommand);*/
		
		watch.stop();
		LOG.info("[trace.station.enter] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	/**
	 * 到站停稳消息处理：根据车次时刻表向客户端发送当前车站的站停时间。
	 * @param in
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@RabbitListener(queues = "#{queueTraceStationArrive.name}")
	public void receiveTraceStationArrive(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.arrive] '" + in + "'");
		TrainEventPosition event = null;
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		event = objMapper.readValue(in, TrainEventPosition.class);
		
		// 检查该车是否有记录
		Integer carNum = (int) event.getCarNum();
		
		//添加列车到站信息
		runTaskService.updateMapTrace(carNum, event);
				
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		String dsStationNum = event.getDstStationNum();
		
		//-------获取或 更新运行图任务信息
		runTaskService.clearMapRuntask(carNum, tablenum);
		getRuntask(carNum, tablenum, trainnum, dsStationNum);
		
		TrainRunTask task = runTaskService.getMapRuntask(carNum);
		
		// 向客户端发送站停时间
		AppDataStationTiming appDataStationTiming = null;

		if (task != null) {
			appDataStationTiming = runTaskService.appDataStationTiming(task, event);
			
			LOG.info("[trace.station.arrive] AppDataTimeStationStop: this station ["
					+ appDataStationTiming.getStation_id() + "] section stop time ["
					+ appDataStationTiming.getTime()
					+ "s]");
			
			sender.senderAppDataStationTiming(appDataStationTiming);
		}
		else {
			LOG.info("[trace.station.arrive] unplanTrain----");
			appDataStationTiming = runTaskService.appDataStationTimingUnplan(event);
			sender.senderAppDataStationTiming(appDataStationTiming);
			//LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}		
		
		/*LOG.info("[trace.station.arrive] AppDataTimeStationStop: this station ["
				+ appDataStationTiming.getStation_id() + "] section stop time ["
				+ appDataStationTiming.getTime()
				+ "s]");
		
		sender.senderAppDataStationTiming(appDataStationTiming);*/
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	
	/**
	 * 离开折返轨消息处理(列车换端时，给尾端发送的AOD命令)：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceReturnLeave.name}")
	public void receiveTraceReturnLeave(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.return.leave] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		TrainEventPosition returnLeaveEvent = null;
		
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			returnLeaveEvent = objMapper.readValue(in, TrainEventPosition.class);
			
			Integer carNum = (int) returnLeaveEvent.getCarNum();
			short tablenum = returnLeaveEvent.getServiceNum();
			short trainnum = returnLeaveEvent.getTrainNum();
			String dsStationNum = returnLeaveEvent.getDstStationNum();
			
			if(tablenum != 0){
				//获取当前车组号对应的运行任务
				//------------获取或 更新运行图任务信息
				runTaskService.clearMapRuntask(carNum, tablenum);
				getRuntask(carNum, tablenum, trainnum, dsStationNum);
				
				TrainRunTask task = runTaskService.getMapRuntask(carNum);
				
				if(task != null){
					// 向该车发送表号、车次号
					AppDataATOCommand appDataATOCommand = null;
					appDataATOCommand = runTaskService.aodCmdReturn(task);
					sender.sendATOCommand(appDataATOCommand);
				}
				/*else{
					//需要发报警信息
					trainrungraphHystrixService.senderAlarmEvent("没有找到车组号为:"+carNum+"表号为:"+tablenum+"车次号为:"+trainnum+"的运行任务");
					LOG.error("[trace.return.leave] serv31-trainrungraph fallback runtask is null!");
				}*/
			}
			else{
				LOG.info("[trace.return.leave] unplanTrain--------");
				//AppDataATOCommand appDataATOCommand = null;
				//appDataATOCommand = runTaskService.aodCmdEnterUnplan(returnLeaveEvent);
				//sender.sendATOCommand(appDataATOCommand);
				LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[trace.return.leave] 消息处理出错!");
		}
		
		watch.stop();
		LOG.info("[trace.return.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	
	/**
	 * 根据车组号、表号和车次号获取列车运行任务信息
	 * @param carNum
	 * @param tablenum
	 * @param trainnum
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public void getRuntask(int carNum, short tablenum, short trainnum, String dsStationNum) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objMapper = new ObjectMapper();
		if((runTaskService.mapRunTask.size() == 0 && tablenum != 0
			|| runTaskService.mapRunTask.size() > 0 && runTaskService.mapRunTask.containsKey(carNum)
			&& runTaskService.mapRunTask.get(carNum).getTrainnum()!= trainnum && tablenum != 0) //&& !"ZH".equals(dsStationNum)
			){//任务列表为空，且该车为计划车时，从运行图服务中获取任务列表
			//if(tablenum != 0){
				String resultMsg = trainrungraphHystrixService.getRuntask(carNum, tablenum, trainnum);
				if(resultMsg != null && !resultMsg.equals("error")){
					TrainRunTask newtask = null;
					try{
						newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
					}catch (Exception e) {
						LOG.error("[trace.station.enter] runtask parse error!");
						//e.printStackTrace();
					}
					if (!runTaskService.mapRunTask.containsKey(carNum)) {
						runTaskService.mapRunTask.put(carNum, newtask);
					}
					else {
						runTaskService.mapRunTask.replace(carNum, newtask);
					}
				}else if(resultMsg == null){
					//需要发报警信息
					trainrungraphHystrixService.senderAlarmEvent("没有找到车组号为:"+carNum+"表号为:"+tablenum+"车次号为:"+trainnum+"的运行任务");
					LOG.error("[trace.station.enter] serv31-trainrungraph fallback runtask is null!");
				}
			//}
		}
	}
}
