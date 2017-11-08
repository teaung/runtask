package com.byd5.ats.rabbitmq;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收并处理识别跟踪的到站消息和离站消息
 * 
 */
public class ReceiverTrace {
	
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverTrace.class);
		
	@Autowired
	private RunTaskService runTaskService;
	
	@Autowired
	private SenderDepart sender;
	
	/**
	 * 到站(不管是否停稳)消息处理：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 */
	@RabbitListener(queues = "#{queueTraceStationEnter.name}")
	public void receiveTraceStationEnter(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.enter] '" + in + "'");
		//doWork(in);
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
		
		//添加列车到站信息
		runTaskService.updateMapTrace(event);
		
		//获取或 更新运行图任务信息
		TrainRunTask task = runTaskService.getMapRuntask(event);
		
		// 向该车发送站间运行等级
		AppDataATOCommand appDataATOCommand = null;
		if(event.getServiceNum() != 0 && task != null){//计划车
			appDataATOCommand = runTaskService.aodCmdEnter(task, event);
		}
		sender.sendATOCommand(appDataATOCommand);
		watch.stop();
		LOG.info("[trace.station.enter] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	/**
	 * 到站停稳消息处理：根据车次时刻表向客户端发送当前车站的站停时间。
	 * @param in
	 */
	@RabbitListener(queues = "#{queueTraceStationArrive.name}")
	public void receiveTraceStationArrive(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.arrive] '" + in + "'");
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
		
		//获取或 更新运行图任务信息
		TrainRunTask task = runTaskService.getMapRuntask(event);
		
		// 向客户端发送站停时间
		AppDataStationTiming appDataStationTiming = null;
				
		if(event.getServiceNum() != 0 && task != null){//计划车
			appDataStationTiming = runTaskService.appDataStationTiming(task, event);
		}		
		
		if(event.getServiceNum() != 0){//非计划车
			appDataStationTiming = runTaskService.appDataStationTimingUnplan(event);
		}
		
		sender.senderAppDataStationTiming(appDataStationTiming);//发送发车倒计时消息
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	
	/**
	 * 离开折返轨消息处理(列车换端时，给尾端发送的AOD命令)：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceReturnLeave.name}")
	public void receiveTraceReturnLeave(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.return.leave] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runTaskService.getMapRuntask(event);
			
			if(event.getServiceNum() != 0 && task != null){//计划车
				AppDataATOCommand appDataATOCommand = runTaskService.aodCmdReturn(event, task);
				sender.sendATOCommand(appDataATOCommand);
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOG.error("[trace.return.leave] 消息处理出错!");
		}
		
		watch.stop();
		LOG.info("[trace.return.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 到达转换轨时，保存列车位置信息
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceTransformArrive.name}")
	public void receiveTraceTransformArrive(String in) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.transform.arrive] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			
			//添加列车到站信息
			runTaskService.updateMapTrace(event);
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOG.error("[trace.transform.arrive] 消息处理出错!");
		}
		
		watch.stop();
		LOG.info("[trace.transform.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
}
