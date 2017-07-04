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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.protocol.ats_vobc.FrameATOCommand;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
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
	
	/**
	 * 到站消息处理：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceStationArrive.name}")
	public void receiveTraceStationArrive(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.debug("[trace.station.arrive] '" + in + "'");
		//System.out.println("[trace] '" + in + "'");
		//doWork(in);
		TrainEventPosition event = null;
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			event = objMapper.readValue(in, TrainEventPosition.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 检查该车是否有记录
		Integer carNum = (int) event.getCarNum();
		TrainRunTask task = null;
		if (runTaskService.mapRunTask.containsKey(carNum)) {
			task = runTaskService.mapRunTask.get(carNum);
		}
		
		// 向该车发送站间运行等级
		FrameATOCommand frameATOCommand = null;

		if (task != null) {
			frameATOCommand = runTaskService.frameATOCommandArrive(task, event);
	
			LOG.debug("[trace.station.arrive] ATOCommand: next station ["
					+ frameATOCommand.getAtoCommand().getNextStationId() + "] section run time ["
					+ frameATOCommand.getAtoCommand().getSectionRunLevel()
					+ "s]");
			sender.sendATOCommand(frameATOCommand);
		}
		else {
			LOG.debug("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}
		
		// 向战场图发送站停时间
		AppDataStationTiming appDataStationTiming = null;

		if (task != null) {
			appDataStationTiming = runTaskService.clientTimeStationStop(task, event);
	
			LOG.debug("[trace.station.arrive] AppDataTimeStationStop: this station ["
					+ appDataStationTiming.getStation_id() + "] section stop time ["
					+ appDataStationTiming.getTime()
					+ "s]");
			sender.sendATOCommand(frameATOCommand);
		}
		else {
			LOG.debug("[trace.station.arrive] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}		

		watch.stop();
		LOG.debug("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	/**
	 * 离站消息处理：根据车次时刻表向VOBC发送命令指定下一个车站是跳停/站停（站停时间）。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceStationLeave.name}")
	public void receiveTraceStationLeave(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.debug("[trace.station.leave] '" + in + "'");
		//System.out.println("[trace] '" + in + "'");
		//doWork(in);
		TrainEventPosition event = null;
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			event = objMapper.readValue(in, TrainEventPosition.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 检查该车是否有记录
		short carNum = event.getTrainNum();
		TrainRunTask task = null;
		if (runTaskService.mapRunTask.containsKey(carNum)) {
			task = runTaskService.mapRunTask.get(carNum);
		}
		
		// 向该车发送下一停站站台、站停时间
		FrameATOCommand frameATOCommand = null;
		if (task != null) {
			frameATOCommand = runTaskService.frameATOCommandLeave(task, event);

			LOG.debug("[trace.station.leave] ATOCommand: next station ["
					+ frameATOCommand.getAtoCommand().getNextStationId() + "] stop time ["
					+ frameATOCommand.getAtoCommand().getStationStopTime()
					+ "s]");
			sender.sendATOCommand(frameATOCommand);
		}
		else {
			LOG.debug("[trace.station.leave] not find the car (" + carNum + ") in runTask list, so do nothing.");
		}
				
		watch.stop();
		LOG.debug("[trace.station.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
}
