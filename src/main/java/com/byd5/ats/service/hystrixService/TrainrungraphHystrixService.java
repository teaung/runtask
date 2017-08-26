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

@Service("TrainrungraphHystrixService")
public class TrainrungraphHystrixService {

	private static final Logger logger = LoggerFactory.getLogger(TrainrungraphHystrixService.class);
	
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
	 * 获取所有车站站停时间
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetRuntaskAllCommand",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getRuntaskAllCommand() throws JsonParseException, JsonMappingException, IOException {
		String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
		return resultMsg;	
	}
	
	public String fallbackGetRuntaskAllCommand() throws AmqpException, JsonProcessingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(5203, sdf.format(new Date()), (long)39, "运行任务：获取所有站台停站时间失败，运行图服务故障!", "39");
		template.convertAndSend(trainrungraphTopic.getName(), alarmKey, mapper.writeValueAsString(alarmEvent));
		logger.error("[getAllStopTime] serv31-trainrungraph can't connetc, or runtask parse error!");
		return "error";
	}
	
	
	/**
	 * 保存设置停站时间命令
	 */
	@HystrixCommand(fallbackMethod = "fallbackSaveRuntaskCommand",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String saveRuntaskCommand(String commandStr) throws JsonParseException, JsonMappingException, IOException {
		String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/saveRuntaskCommand?json={json}", String.class, commandStr);
		return resultMsg;	
	}
	
	public String fallbackSaveRuntaskCommand(String commandStr) throws AmqpException, JsonProcessingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(5203, sdf.format(new Date()), (long)39, "运行任务：设置站台停站时间失败，运行图服务故障!", "39");
		template.convertAndSend(trainrungraphTopic.getName(), alarmKey, mapper.writeValueAsString(alarmEvent));
		logger.error("[setDwellTime] serv31-trainrungraph connetc error!");
		return "error";
	}
	
	
	/**
	 * 获取运行图运行任务
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetRuntask",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getRuntask(Integer groupnum, short tablenum, short trainnum) throws JsonParseException, JsonMappingException, IOException {
		String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={groupnum}&tablenum={tablenum}&trainnum={trainnum}", String.class, groupnum, tablenum, trainnum);
		return resultMsg;	
	}
	
	public String fallbackGetRuntask(Integer groupnum, short tablenum, short trainnum) throws AmqpException, JsonProcessingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(5203, sdf.format(new Date()), (long)39, "运行任务：获取列车运行任务失败，运行图服务故障!", "39");
		template.convertAndSend(trainrungraphTopic.getName(), alarmKey, mapper.writeValueAsString(alarmEvent));
		logger.error("[getRuntask] serv31-trainrungraph connetc error!");
		return "error";
	}
}
