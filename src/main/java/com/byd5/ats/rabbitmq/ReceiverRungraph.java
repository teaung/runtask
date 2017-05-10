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

import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.protocol.ats_ci.FrameCIStatus;
import com.byd5.ats.protocol.ats_vobc.AppDataATOCommand;
import com.byd5.ats.protocol.ats_vobc.FrameATOCommand;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	public void receiveRungraph(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		//System.out.println("[rungraph] '" + in + "'");
		//doWork(in);
		LOG.debug("[rungraph] '" + in + "'");
		
		TrainRunTask task = null;
		TrainRunTimetable timetable = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			task = objMapper.readValue(in, TrainRunTask.class);
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
		FrameATOCommand frameATOCommand = null;
		try {
			frameATOCommand = runTaskHandler.frameATOCommandTask(task);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sender.sendATOCommand(frameATOCommand);
		
		
		//timetable = task.getRungraphTodayTimetable().get(0);
		//System.out.println("[rungraph] send timetiable ...");
		//sender.sendDepart(timetable);
		
		watch.stop();
		//System.out.println("[rungraph] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/*@RabbitListener(queues = "#{autoDeleteQueue1.name}")
	public void receive1(String in) throws InterruptedException {
		receive(in, 1);
		atsWebsocket.sendMessageToUsers(new TextMessage("[1] " +in));
	}

	@RabbitListener(queues = "#{autoDeleteQueue2.name}")
	public void receive2(String in) throws InterruptedException {
		receive(in, 2);
		atsWebsocket.sendMessageToUsers(new TextMessage("[2] " +in));
	}*/

/*	public void receive(String in, int receiver) throws InterruptedException {
		StopWatch watch = new StopWatch();
		watch.start();
		System.out.println("instance " + receiver + " [x] Received '" + in + "'");
		doWork(in);
		watch.stop();
		System.out.println("instance " + receiver + " [x] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	private void doWork(String in) throws InterruptedException {
		for (char ch : in.toCharArray()) {
			if (ch == '.') {
				Thread.sleep(1000);
			}
		}
	}*/

}
