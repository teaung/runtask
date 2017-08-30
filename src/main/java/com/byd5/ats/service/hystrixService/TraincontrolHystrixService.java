package com.byd5.ats.service.hystrixService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.message.ATSAlarmEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service("TraincontrolHystrixService")
public class TraincontrolHystrixService{

	private static final Logger logger = LoggerFactory.getLogger(TraincontrolHystrixService.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private RabbitTemplate template;
	
	@Autowired
	@Qualifier("exchangeRungraph")
	private TopicExchange trainrungraphTopic;
	
	private String alarmKey = "ats.trainrungraph.alert";
	
	/**
	 * 获取当前站台跳停状态
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetSkipStationStatus",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getSkipStationStatus(Integer platformId) throws JsonParseException, JsonMappingException, IOException {
		String skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
		return skipStatusStr;	
	}
	
	public String fallbackGetSkipStationStatus(Integer platformId) throws AmqpException, JsonProcessingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(5203, sdf.format(new Date()), (long)39, "运行任务：获取站台"+platformId+"是否跳停状态失败，列车控制服务故障!", "39");
		String alarmStr = mapper.writeValueAsString(alarmEvent);
		template.convertAndSend(trainrungraphTopic.getName(), alarmKey, alarmStr);
		logger.error("[x] AlarmEvent: "+alarmStr);
		logger.error("[getSkipStationStatus] serv35-traincontrol connetc error!");
		return "error";
	}
	
	
}
