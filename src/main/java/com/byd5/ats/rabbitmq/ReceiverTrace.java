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

import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
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
		Integer carNum = (int) event.getCargroupNum();
		
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		String dsStationNum = event.getDstCode();
		
		//-------------(1)添加列车到站信息
		runTaskService.updateMapTrace(carNum, event);
				
		//-------------(2)清除计划车运行图任务信息(非计划车)
		runTaskService.clearMapRuntask(carNum, tablenum);//非计划车时，移除残留的计划车运行任务信息
		
		//-------------(3)根据车组号、表号和车次号获取列车运行任务信息
		runTaskService.getRuntask(carNum, tablenum, trainnum);
		TrainRunTask task = runTaskService.getMapRuntask(carNum);
		
		//-------------(4)停站时间列表为空，则查询数据库获取--------------
		if(runTaskService.mapDwellTime.size() == 0){
			try{
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
		
		
		AppDataAVAtoCommand appDataAVAtoCommand = null;//ATO命令

		if (task != null) {//计划车
			appDataAVAtoCommand = runTaskService.aodCmdEnter(task, event);
			
			if(appDataAVAtoCommand.getNextSkipCmd() == 0x55){//若列车下一站有跳停，则连续给车发3次命令
				sender.sendATOCommand(appDataAVAtoCommand);
				sender.sendATOCommand(appDataAVAtoCommand);
			}
			sender.sendATOCommand(appDataAVAtoCommand);
			LOG.info("[trace.station.enter] ATOCommand: next station ["
					+ appDataAVAtoCommand.getNextStopPlatformId() + "] section run time ["
					+ appDataAVAtoCommand.getSectionRunAdjustCmd()+ "s]"
					+ "section stop time ["+ appDataAVAtoCommand.getPlatformStopTime()
					+ "s]");
		}
		else {//非计划车到站时的处理
			LOG.info("[trace.station.enter] unplanTrain----");
			//appDataATOCommand = runTaskService.aodCmdEnterUnplan(event);
			LOG.info("[trace.station.enter] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}
		
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
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		event = objMapper.readValue(in, TrainEventPosition.class);
		
		// 检查该车是否有记录
		Integer carNum = (int) event.getCargroupNum();
		
				
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		String dsStationNum = event.getDstCode();
		
		// -------------(1)添加列车到站信息
		runTaskService.updateMapTrace(carNum, event);

		// -------------(2)清除计划车运行图任务信息(非计划车)
		runTaskService.clearMapRuntask(carNum, tablenum);// 非计划车时，移除残留的计划车运行任务信息

		// -------------(3)根据车组号、表号和车次号获取列车运行任务信息
		runTaskService.getRuntask(carNum, tablenum, trainnum);
		TrainRunTask task = runTaskService.getMapRuntask(carNum);
		
		// 向客户端发送站停时间
		AppDataStationTiming appDataStationTiming = null;

		if (task != null) {
			appDataStationTiming = runTaskService.appDataStationTiming(task, event);
			sender.senderAppDataStationTiming(appDataStationTiming);
			
			LOG.info("[trace.station.arrive] AppDataTimeStationStop: this station ["
					+ appDataStationTiming.getStation_id() + "] section stop time ["
					+ appDataStationTiming.getTime()
					+ "s]");
		}
		else {
			LOG.info("[trace.station.arrive] unplanTrain----");
			appDataStationTiming = runTaskService.appDataStationTimingUnplan(event);
			sender.senderAppDataStationTiming(appDataStationTiming);
			//LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}		
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	
	/**
	 * 列车到达转换轨时，保存列车位置信息
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceTransformArrive.name}")
	public void receiveTraceTransformArrive(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.transform.arrive] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			Integer carNum = (int) event.getCargroupNum();
			
			//添加列车到站信息
			runTaskService.updateMapTrace(carNum , event);
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[trace.transform.arrive] 消息处理出错!");
		}
		
		watch.stop();
		LOG.info("[trace.transform.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
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
		
		TrainEventPosition returnLeaveEvent = null;
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			returnLeaveEvent = objMapper.readValue(in, TrainEventPosition.class);
			
			Integer carNum = (int) returnLeaveEvent.getCargroupNum();
			short tablenum = returnLeaveEvent.getServiceNum();
			short trainnum = returnLeaveEvent.getTrainNum();
			String dsStationNum = returnLeaveEvent.getDstCode();
			
			if(tablenum != 0){
				//获取当前车组号对应的运行任务
				//------------获取或 更新运行图任务信息
				runTaskService.clearMapRuntask(carNum, tablenum);
				runTaskService.getRuntask(carNum, tablenum, trainnum);
				
				TrainRunTask task = runTaskService.getMapRuntask(carNum);
				
				if(task != null){
					// 向该车发送表号、车次号
					AppDataAVAtoCommand appDataAVAtoCommand = null;
					appDataAVAtoCommand = runTaskService.aodCmdReturn(returnLeaveEvent, task);
					sender.sendATOCommand(appDataAVAtoCommand);
				}
			}
			else{
				LOG.info("[trace.return.leave] unplanTrain--------");
				//AppDataATOCommand appDataATOCommand = runTaskService.aodCmdEnterUnplan(returnLeaveEvent);
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
	
}
