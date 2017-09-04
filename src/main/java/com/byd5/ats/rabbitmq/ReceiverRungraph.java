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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunInfo;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收运行图的消息
 * 
 */
public class ReceiverRungraph {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverRungraph.class);
	
/*	@Autowired
	private AtsWebSocketHandler atsWebsocket;*/
	
	@Autowired
	private RunTaskService runTaskHandler;
	
	@Autowired
	private SenderDepart sender;

	@RabbitListener(queues = "#{queueRungraph.name}")
	public void receiveRungraph(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		//System.out.println("[rungraph] '" + in + "'");
		//doWork(in);
		LOG.info("[rungraph] '" + in + "'");
		
		TrainRunTask task = null;
		TrainRunTimetable timetable = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		//objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try{
			task = objMapper.readValue(in, TrainRunTask.class);
			
			// 添加运行任务列表
			//runTaskHandler.runTaskList.add(task);
			Integer carNum = task.getTraingroupnum();
			if (!runTaskHandler.mapRunTask.containsKey(carNum)) {
				runTaskHandler.mapRunTask.put(carNum, task);
			}
			else {
				runTaskHandler.mapRunTask.replace(carNum, task);
			}
			
			// 向该车发送表号、车次号
			AppDataATOCommand appDataATOCommand = null;
			appDataATOCommand = runTaskHandler.appDataATOCommandTask(task);
			
			sender.sendATOCommand(appDataATOCommand);
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[rungraph runtask] parse data error!");
		}
		
		
		watch.stop();
		System.out.println("[rungraph] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 列车出入段时，表号、车次号、车组号信息
	 * @param in
	 * @throws Exception
	 */
	@RabbitListener(queues = "#{queueRungraphRunInfo.name}")
	public void receiveRungraphRunInfo(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[rungraph RunInfo] '" + in + "'");
		
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainRunInfo trainRunInfo = objMapper.readValue(in, TrainRunInfo.class);
			
			// 向该车发送表号、车次号
			AppDataATOCommand appDataATOCommand = null;
			appDataATOCommand = runTaskHandler.appDataATOCommandTask(trainRunInfo);
			
			sender.sendATOCommand(appDataATOCommand);
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[rungraph RunInfo] parse data error!");
		}
		
		
		watch.stop();
		LOG.info("[rungraph RunInfo] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 当列车车次号变更时，收到运行图发来的新车次时刻表后，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueRungraphChangeTask.name}")
	public void receiveRungraphChangeTask(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[rungraph.changeTask] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		TrainEventPosition returnLeaveEvent = null;
		TrainRunTask task = null;
		
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			task = objMapper.readValue(in, TrainRunTask.class);
			
			if(task != null){
				// 向该车发送表号、车次号
				AppDataATOCommand appDataATOCommand = null;
				appDataATOCommand = runTaskHandler.appDataATOCommandTask(task);
				sender.sendATOCommand(appDataATOCommand);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[rungraph.changeTask] 消息处理出错!");
		}
		
		watch.stop();
		LOG.info("[rungraph.changeTask] Done in " + watch.getTotalTimeSeconds() + "s");
	}
}
