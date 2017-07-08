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

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.protocol.ats_vobc.FrameATOStatus;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收运行图的消息
 * 
 */
public class ReceiverATOStatus {/*
		
	@Autowired
	private RunTaskService runTaskHandler;
	
	@RabbitListener(queues = "#{queueATOStatus.name}")
	public void receive(String in) throws InterruptedException {
		StopWatch watch = new StopWatch();
		watch.start();
//		System.out.println("[atostatus] Received '" + in + "'");
		//doWork(in);
		
		FrameATOStatus frame = null;
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			frame = objMapper.readValue(in, FrameATOStatus.class);
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
		//runTaskHandler.frameATOStatus = frame;
		int carNum = frame.getAtoStatus().getCarNum();
		if (!runTaskHandler.mapATOStatus.containsKey(carNum)) {
			runTaskHandler.mapATOStatus.put(carNum, frame);
		}
		else {
			runTaskHandler.mapATOStatus.replace(carNum, frame);
		}
		
		watch.stop();
//		System.out.println("[atostatus] Done in " + watch.getTotalTimeSeconds() + "s");
	}

*/}
