package com.byd5.ats.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.rabbitmq.ReceiverAdjust;
import com.byd5.ats.rabbitmq.ReceiverTrace;
import com.byd5.ats.service.RunTaskService;
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
	@Autowired
	ReceiverAdjust receiverAdjust;
	@Autowired
	private RunTaskService runTaskHandler;
	
	/*** (计划车)列车到站（未停稳）测试*/
	@RequestMapping(value = "/stationEnter")
	public void stationEnter(@RequestParam short carNum, @RequestParam short trainNum
			,@RequestParam int station) throws JsonParseException, JsonMappingException, IOException{
		event.setServiceNum((short) 1);
		event.setTrainNum(trainNum);
		event.setCargroupNum(carNum);
		event.setTimestamp(new Date().getTime());
		event.setStation(station);
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
		event.setCargroupNum((short) 102);
		event.setTimestamp(new Date().getTime());
		event.setStation(6);
		event.setNextStationId(2);
		
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
		event.setStation(6);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationLeave(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车离开折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnLeave")
	public void returnLeave() throws Exception{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
		log.info("处理完成：");
	}
	
	/*** (计划车)列车到达折返轨测试
	 * @throws Exception */
	@RequestMapping(value = "/returnArrive")
	public void returnArrive() throws Exception{
		event.setServiceNum((short) 1);
		event.setTrainNum((short) 102);
		event.setCargroupNum((short) 102);
		event.setTimestamp(1505520000000L);
		event.setStation(1);
		event.setNextStationId(2);
		
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
		event.setStation(1);
		event.setNextStationId(2);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceReturnLeave(json);
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
		event.setStation(6);
		event.setNextStationId(3);
		
		TrainRunTask task = runTaskHandler.getMapRuntask(event);
		
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
		//添加列车到站信息
		runTaskHandler.updateMapTrace(event);
				
		String json = mapper.writeValueAsString(task);
		receiverAdjust.receiveAdjust(json);
		log.info("处理完成：");
	}
}
