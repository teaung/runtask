package com.byd5.ats.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.byd.ats.protocol.ats_vobc.AppDataATOCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 
 * @author wu.xianglan
 *
 */
/*@Component
@Configurable
@EnableScheduling*/
public class test{
	
	private static final Logger logger = LoggerFactory.getLogger(test.class);
	
	ObjectMapper mapper = new ObjectMapper(); // 转换器
	
	@Autowired
	private RabbitTemplate template;
	
	@Autowired
	@Qualifier("exchangeDepart")
	private TopicExchange exchangeDepart;
	
	String realtimeKey = "ats.traindepart.aod.command";
    //每1分钟执行一次
    //@Scheduled(cron = "0 */1 *  * * * ")
	//@Scheduled(fixedRate = 1000)
	public void senderAppDataStationTiming() {
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		AppDataATOCommand appDataATOCommand = new AppDataATOCommand();
		
		String js = null;
		try {
			js = objMapper.writeValueAsString(appDataATOCommand);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		template.convertAndSend(exchangeDepart.getName(), realtimeKey, js);
		logger.debug("[departX] " + exchangeDepart.getName() + ":" + realtimeKey + " '" + js + "'");
	}
}
