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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.protocol.AppProtocolConstant;
import com.byd5.ats.protocol.ats_vobc.FrameATOCommand;
import com.byd5.ats.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Gary Russell
 * @author Scott Deeg
 */
public class SenderDepart {
	private final static Logger LOG = LoggerFactory.getLogger(SenderDepart.class);

	@Value("${ats.serv.tag:0.0.0.0}")
	private String tag = "";

	@Autowired
	private RabbitTemplate template;

	@Autowired
	@Qualifier("exchangeDepart")
	private TopicExchange topic;

	@Autowired
	@Qualifier("topicATS2CU")
	private TopicExchange exATS2CU;
	
	@Autowired
	@Qualifier("topicServ2Cli")
	private TopicExchange exServ2Cli;
	
	String realtimeKey = "serv2cli.trainruntask.realtime";
	
	//public final static String SERVID = "traindepart" + UUID.randomUUID().toString().replace("-", "");
	public final static String SERVID = "traindepart(" + Utils.getLocalIP() + ")";

	
	private int index;

	private int count;

	/*private final String[] keys = {"quick.orange.rabbit", "lazy.orange.elephant", "quick.orange.fox",
			"lazy.brown.fox", "lazy.pink.rabbit", "quick.brown.fox"};*/

	/*@Scheduled(fixedDelay = 1000, initialDelay = 500)
	public void send() {
		StringBuilder builder = new StringBuilder("Hello to ");
		if (++this.index == keys.length) {
			this.index = 0;
		}
		String key = keys[this.index];
		builder.append(key).append(' ');
		builder.append(Integer.toString(++this.count));
		String message = builder.toString();
		template.convertAndSend(topic.getName(), key, message);
		System.out.println(" [x] Sent '" + message + "'");
	}*/

/*	public void send(String msg) {
		//StringBuilder builder = new StringBuilder("Hello to ");
		StringBuilder builder = new StringBuilder("'" + msg + "' ");
		if (++this.index == keys.length) {
			this.index = 0;
		}
		String key = keys[this.index];
		builder.append(key).append(' ');
		builder.append(Integer.toString(++this.count));
		String message = builder.toString();
		template.convertAndSend(topic.getName(), key, message);
		System.out.println(" [x] Sent '" + message + "'");
	}*/
	
	public void sendDepart(TrainRunTimetable table) {
		
		table.servTag = this.tag;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		//objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			//s = objMapper.writeValueAsString(ciStatus);
			js = objMapper.writeValueAsString(table);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(js);
		String routeKey = "ats.traindepart.timetable";
		template.convertAndSend(topic.getName(), routeKey, js);
		//System.out.println("[departS] '" + js + "'");
		LOG.debug("[departX] " + topic.getName() + ":" + routeKey + " '" + js + "'");
	}
	
	
	public void sendATOCommand(FrameATOCommand fCommand) {
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			//s = objMapper.writeValueAsString(ciStatus);
			js = objMapper.writeValueAsString(fCommand);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(js);
		String routeKey = AppProtocolConstant.ROUTINGKEY_VOBC_ATO_COMMAND; //"ats2cu.vobc.command";
		template.convertAndSend(exATS2CU.getName(), routeKey, js);
		//System.out.println("[departX] '" + js + "'");
		LOG.debug("[departX] " + exATS2CU.getName() + ":" + routeKey + " '" + js + "'");
	}
	
	/**
	 * 给客户端发送当前站停时间
	 * @param appDataStationTiming
	 */
	public void senderAppDataStationTiming(AppDataStationTiming appDataStationTiming) {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		String js = null;
		try {
			js = objMapper.writeValueAsString(appDataStationTiming);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		template.convertAndSend(exServ2Cli.getName(), realtimeKey, js);
		LOG.debug("[departX] " + exServ2Cli.getName() + ":" + realtimeKey + " '" + js + "'");
	}
}
