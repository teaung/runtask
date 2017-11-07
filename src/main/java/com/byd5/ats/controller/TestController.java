package com.byd5.ats.controller;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.rabbitmq.ReceiverTrace;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Component
public class TestController{

	private static final Logger log = LoggerFactory.getLogger(TestController.class);

	private TrainEventPosition event = new TrainEventPosition();
	
	private ObjectMapper mapper = new ObjectMapper(); // 转换器

	@Autowired
	ReceiverTrace ReceiverTrace;
	
	/*** (计划车)列车到站（未停稳）测试*/
	@RequestMapping(value = "/stationEnter")
	public void stationEnter() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationEnter(json);
		log.info("处理完成：");
	}

	/*** (计划车)列车到站（停稳）测试*/
	@RequestMapping(value = "/stationArrive")
	public void stationArrive() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationArrive(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车离开折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnLeave")
	public void returnLeave() throws Exception{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
		log.info("处理完成：");
	}
	
	
	/*** (非计划车)列车到站（未停稳）测试*/
	@RequestMapping(value = "/stationEnterUnplan")
	public void stationEnterUnplan() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationEnter(json);
		log.info("处理完成：");
	}

	/*** (非计划车)列车到站（停稳）测试*/
	@RequestMapping(value = "/stationArriveUnplan")
	public void stationArriveUnplan() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationArrive(json);
		log.info("处理完成：");
	}
	
	/*** (非计划车)列车离开折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnLeaveUnplan")
	public void returnLeaveUnplan() throws Exception{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCarNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
		log.info("处理完成：");
	}
}
