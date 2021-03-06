package com.byd5.ats.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.rabbitmq.ReceiverAdjust;
import com.byd5.ats.rabbitmq.ReceiverTrace;
import com.byd5.ats.service.TrainRuntaskService;
import com.byd5.ats.service.hystrixService.TraincontrolHystrixService;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
@Component
public class TestController{

	private static final Logger log = LoggerFactory.getLogger(TestController.class);

	private TrainEventPosition event = new TrainEventPosition();
	private ObjectMapper mapper = new ObjectMapper(); // 转换器

	@Autowired
	TraincontrolHystrixService traincontrolHystrixService;
	@Autowired
	ReceiverTrace ReceiverTrace;
	@Autowired
	ReceiverAdjust receiverAdjust;
	@Autowired
	private TrainRuntaskService trainRuntaskService;
	@Autowired
	RuntaskUtils runtaskUtils;
	
	/*** (计划车)列车到站（未停稳）测试*/
	@RequestMapping(value = "/stationEnter")
	public void stationEnter() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short)1);
		event.setTrainNum(102);
		event.setCargroupNum(102);
		event.setTimestamp(new Date().getTime());
		event.setDstCode("AI");
		event.setStation(9);
		event.setNextStationId(7);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationEnter(json);
		log.info("处理完成：");
	}

	/*** (计划车)列车到站（停稳）测试*/
	@RequestMapping(value = "/stationArrive")
	public void stationArrive() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(new Date().getTime());
		event.setDstCode("ZF");
		event.setStation(3);
		event.setNextStationId(4);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationArrive(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车离站测试*/
	@RequestMapping(value = "/stationLeave")
	public void stationLeave() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(new Date().getTime());
		event.setDstCode("ZF");
		event.setTrainDir((short) 85);
		event.setStation(6);
		event.setNextStationId(7);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationLeave(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车离开折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnLeave")
	public void returnLeave() throws Exception{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
		event.setStation(9);
		event.setNextStationId(7);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车到达折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnArrive")
	public void returnArrive() throws Exception{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
		event.setTrainDir((short) 85);
		event.setStation(9);
		event.setNextStationId(7);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnArrive(json);
		log.info("处理完成：");
	}
	
	/*** (非计划车)列车到站（未停稳）测试*/
	@RequestMapping(value = "/stationEnterUnplan")
	public void stationEnterUnplan() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationLeave(json);
		log.info("处理完成：");
	}

	/*** (非计划车)列车到站（停稳）测试*/
	@RequestMapping(value = "/stationArriveUnplan")
	public void stationArriveUnplan() throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
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
		event.setCargroupNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
		event.setStation(9);
		event.setNextStationId(7);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
		log.info("处理完成：");
	}
	
	/*** (非计划车)列车离开折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/transformArriveUnplan")
	public void transformArriveUnplan() throws Exception{
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 103);
		event.setTimestamp(1505520000000L);
		event.setDstCode("ZF");
		event.setStation(10);
		event.setNextStationId(9);
		event.setTrainDir((short) 0x55);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceTransformArrive(json);
		log.info("处理完成：");
	}
	
	
	/*** 早晚点测试
	 * @throws Exception */
	@RequestMapping(value = "/arriveAdust")
	public void arriveAdust() throws Exception{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1511824717000L);
		event.setDstCode("ZF");
		event.setStation(4);
		event.setNextStationId(5);
		
		TrainRunTask task = runtaskUtils.getMapRuntask(event);
		
		int platformId = event.getStation();
		TrainRunTimetable currStation = null;
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		for (int i = 1; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				event.setTimestamp(currStation.getPlanLeaveTime());
				currStation.setPlanLeaveTime(currStation.getPlanLeaveTime() + 10*1000);
				timetableList.set(i, currStation);
				task.setTrainRunTimetable(timetableList);
				break;
			}
		}
				
		String json = mapper.writeValueAsString(task);
		receiverAdjust.receiveAdjust(json);
		log.info("处理完成：");
	}
	
	/*** 早晚点测试
	 * @throws Exception */
	@RequestMapping(value = "/getAllTrainStatus")
	public void getAllTrainStatus(){
		runtaskUtils.getAllTrainStatus();
	}
	
	@RequestMapping(value = "/getNextPlatformId")
	public void getNextPlatformId(){
		traincontrolHystrixService.getNextPlatformId((short) 170, 7);
	}
	
	@RequestMapping(value = "/setDetain")
	public void setDetain() throws JsonProcessingException{
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		event.setServiceNum((short) 0);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1511824717000L);
		event.setTrainDir((short) 85);
		event.setDstCode(null); 
		event.setStation(3);
		event.setNextStationId(4);
		List<Byte> listDtStatus = runtaskUtils.listDtStatus;//[3, 3, 1, 3, 3, 3, 3, 3]
		listDtStatus.set(2, (byte) 1);
		AppDataAVAtoCommand appDataATOCommand = trainRuntaskService.getStationEnterUnplan(event);
		System.out.println(mapper.writeValueAsString(appDataATOCommand));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	}
}
