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
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd.ats.protocol.ats_vobc.FrameATOCommand;
import com.byd5.ats.service.RunTaskService;
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
	private RestTemplate restTemplate;
	
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
		TrainRunTask task = null;
		
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		
		if(runTaskService.mapRunTask.size() == 0){//任务列表为空，且该车为计划车时，从运行图服务中获取任务列表
			if(tablenum != 0){
				String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, carNum, tablenum, trainnum);
				TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
				if(newtask != null){
					runTaskService.mapRunTask.put(carNum, newtask);
				}else{
					//需要发报警信息
					LOG.error("[trace.station.arrive] get runtask error. runtask not found");
				}
			}
		}
		
		if (runTaskService.mapRunTask.containsKey(carNum)) {
			task = runTaskService.mapRunTask.get(carNum);
		}
		
		//添加列车到站信息
		if (!runTaskService.mapTrace.containsKey(carNum)) {
			runTaskService.mapTrace.put(carNum, event);
		}
		else {
			runTaskService.mapTrace.replace(carNum, event);
		}
		
		
		//---------------停站时间列表为空，则查询数据库获取--------------
		if(runTaskService.mapDwellTime.size() == 0){
			String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
			List<AppDataDwellTimeCommand> dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
			for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
				runTaskService.mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
			}
		}
		
		
		// 向该车发送站间运行等级
		AppDataATOCommand appDataATOCommand = null;

		if (task != null) {//计划车
			appDataATOCommand = runTaskService.appDataATOCommandEnter(task, event);
	
			LOG.info("[trace.station.arrive] ATOCommand: next station ["
					+ appDataATOCommand.getNextStationId() + "] section run time ["
					+ appDataATOCommand.getSectionRunLevel()+ "s]"
					+ "section stop time ["+ appDataATOCommand.getStationStopTime()
					+ "s]");
			sender.sendATOCommand(appDataATOCommand);
		}
		else {//非计划车到站时的处理
			appDataATOCommand = runTaskService.appDataATOCommandEnterUnplan(event);
			LOG.info("[trace.station.arrive] unplanTrain ATOCommand: next station ["
					+ appDataATOCommand.getNextStationId() + "] section run time ["
					+ appDataATOCommand.getSectionRunLevel()+ "s]"
					+ "section stop time ["+ appDataATOCommand.getStationStopTime()
					+ "s]");
			sender.sendATOCommand(appDataATOCommand);
			
			//LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
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
		TrainRunTask task = null;
		
		short tablenum = event.getServiceNum();
		short trainnum = event.getTrainNum();
		
		if(runTaskService.mapRunTask.size() == 0){
			if(tablenum != 0){
				String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, carNum, tablenum, trainnum);
				TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
				if(newtask != null){
					runTaskService.mapRunTask.put(carNum, newtask);
				}else{
					//需要发报警信息
					LOG.error("[trace.station.arrive] get runtask error. runtask not found");
				}
			}
		}
		
		if (runTaskService.mapRunTask.containsKey(carNum)) {
			task = runTaskService.mapRunTask.get(carNum);
		}
		
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
			LOG.info("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}		
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
}
