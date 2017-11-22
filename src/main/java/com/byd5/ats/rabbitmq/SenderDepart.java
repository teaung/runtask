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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.byd.ats.protocol.RabbConstant;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.ATSAlarmEvent;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.utils.RuntaskConstant;
import com.byd5.ats.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * 运行任务发送消息
 * @author wu.xianglan
 */
public class SenderDepart{
	private final static Logger LOG = LoggerFactory.getLogger(SenderDepart.class);

	@Value("${ats.serv.tag:0.0.0.0}")
	private String tag = "";

	@Autowired
	private RabbitTemplate template;

	/*@Autowired
	@Qualifier("exchangeDepart")
	private TopicExchange topic;*/

	@Autowired
	@Qualifier("topicATS2CU")
	private TopicExchange exATS2CU;
	
	@Autowired
	@Qualifier("topicServ2Cli")
	private TopicExchange exServ2Cli;
	
	//public final static String SERVID = "traindepart" + UUID.randomUUID().toString().replace("-", "");
	public final static String SERVID = "traindepart(" + Utils.getLocalIP() + ")";

	public void sendDepart(TrainRunTimetable table) throws JsonProcessingException {
		
		//table.servTag = this.tag;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		String js = null;
		js = objMapper.writeValueAsString(table);
		
		String routeKey = "ats.traindepart.timetable";
		template.convertAndSend(exATS2CU.getName(), routeKey, js);
		LOG.debug("[departX] " + exATS2CU.getName() + ":" + routeKey + " '" + js + "'");
	}
	
	/**
	 * 给AOD发送AOD命令信息帧
	 * @param appDataATOCommand
	 * @throws JsonProcessingException
	 */
	public void sendATOCommand(AppDataAVAtoCommand appDataATOCommand) throws JsonProcessingException {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		String js = null;
		
		if(appDataATOCommand != null){
			/*LOG.info("[trace.station.enter] ATOCommand: next station [{}] section run time [{}s]"
					+ "section stop time [{}s]", appDataATOCommand.getNextStationId()
					, appDataATOCommand.getSectionRunLevel(), appDataATOCommand.getStationStopTime());*/
			js = objMapper.writeValueAsString(appDataATOCommand);

			//String routeKey = AppProtocolConstant.ROUTINGKEY_VOBC_ATO_COMMAND; //"ats2cu.vobc.command";
			String routeKey = RabbConstant.RABB_RK_AV_ATOCMD; //"ats2cu.vobc.command";
			
			if(appDataATOCommand.getNextSkipCmd() == 0x55){//若列车下一站有跳停，则连续给车发3次命令
				template.convertAndSend(exATS2CU.getName(), routeKey, js);
				LOG.info("[departX] " + exATS2CU.getName() + ":" + routeKey + " '" + js + "'");
				template.convertAndSend(exATS2CU.getName(), routeKey, js);
				LOG.info("[departX] " + exATS2CU.getName() + ":" + routeKey + " '" + js + "'");
			}
			
			template.convertAndSend(exATS2CU.getName(), routeKey, js);
			LOG.info("[departX] " + exATS2CU.getName() + ":" + routeKey + " '" + js + "'");
		}
		
	}
	
	/**
	 * 给客户端发送当前车站站停时间
	 * @param appDataStationTiming
	 * @throws JsonProcessingException 
	 */
	public void senderAppDataStationTiming(AppDataStationTiming appDataStationTiming) throws JsonProcessingException {
		
		ObjectMapper objMapper = new ObjectMapper();
		//objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		String js = null;
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(appDataStationTiming != null){
			LOG.info("[trace.station.arrive] AppDataTimeStationStop: this station ["
					+ appDataStationTiming.getStation_id() + "] section stop time ["
					+ appDataStationTiming.getTime() + "s]");
			
			map.put("ats_station_timing", appDataStationTiming);
			js = objMapper.writeValueAsString(map);
			
			
			//js = objMapper.writeValueAsString(appDataStationTiming);
			
			template.convertAndSend(exServ2Cli.getName(), RuntaskConstant.RABB_RK_RUNTASK_REALTIME, js);
			LOG.info("[departX] " + exServ2Cli.getName() + ":" + RuntaskConstant.RABB_RK_RUNTASK_REALTIME + " '" + js + "'");
		}
		
	}
	
	/**
	 * 给告警服务发送告警信息
	 * @param alarmEvent
	 */
	public void senderAlarmEvent(String msg){
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(msg);
		template.convertAndSend(RuntaskConstant.RABB_EX_DEPART, RuntaskConstant.RABB_RK_ALARM_ALERT, alarmEvent.toString());
		LOG.error("[x] AlarmEvent: "+alarmEvent);
	}
}
