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

/**
 * 接收运行图的消息
 * 
 */
public class ReceiverDepart {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverDepart.class);
	
/*	@Autowired
	private AtsWebSocketHandler atsWebsocket;*/

	@RabbitListener(queues = "#{queueDepart.name}")
	public void receiveDepart(String in) throws InterruptedException {
		StopWatch watch = new StopWatch();
		watch.start();
		//System.out.println("[departR] '" + in + "'");
		LOG.info("[departR] '" + in + "'");
		
		//doWork(in);
		watch.stop();
//		System.out.println("[depart] Done in " + watch.getTotalTimeSeconds() + "s");
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

	public void receive(String in, int receiver) throws InterruptedException {
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
	}

}
