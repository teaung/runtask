package com.byd5.ats.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunInfo;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.service.TrainRuntaskService;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收并处理识别跟踪的到站消息和离站消息
 * 
 */
public class ReceiverTrace {
	
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverTrace.class);
		
	@Autowired
	private TrainRuntaskService trainRuntaskService;
	@Autowired
	private SenderDepart sender;
	@Autowired
	private RuntaskUtils runtaskUtils;
	
	
	/**
	 * 列车进站消息处理：根据车次时刻表向VOBC发送命令指定当前区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 */
	@RabbitListener(queues = "#{queueTraceStationEnter.name}")
	public void receiveTraceStationEnter(String in){
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.enter] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runtaskUtils.getMapRuntask(event);
			
			// 向该车发送站间运行等级
			AppDataAVAtoCommand appDataATOCommand = null;
			if(event.getServiceNum() != 0 && task != null){//计划车
				appDataATOCommand = trainRuntaskService.getStationEnter(task, event);
			}
			if(event.getServiceNum() == 0 && event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车(带目的地号)
				appDataATOCommand = trainRuntaskService.getStationEnterUnplan(event);
			}
			if(event.getServiceNum() == 0 && 
					(event.getDstCode() == null || event.getDstCode() != null && "".equals(event.getDstCode()))){//人工车
				short detainCmd = runtaskUtils.getDtStatusCmd(event.getStation());
				if(event.getServiceNum() == 0 && detainCmd == 0x55){
					//appDataATOCommand = runTaskService.getStationEnterUnplan(event);
					appDataATOCommand = trainRuntaskService.getStationDetainUnplan(event, event.getStation());
				}
			}
			sender.sendATOCommand(appDataATOCommand);
		}catch (Exception e) {
			LOG.error("[trace.station.enter] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[trace.station.enter] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 到站(不管是否停稳)消息处理：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 */
	@RabbitListener(queues = "#{queueTraceStationLeave.name}")
	public void receiveTraceStationLeave(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.leave] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			//添加列车到站信息
			////runTaskService.updateMapTrace(event);
			//runTaskService.removeMapTrace(event);
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runtaskUtils.getMapRuntask(event);
			
			// 向该车发送站间运行等级
			AppDataAVAtoCommand appDataATOCommand = null;
			if(event.getServiceNum() != 0 && task != null){//计划车
				appDataATOCommand = trainRuntaskService.getStationLeave(task, event);
			}
			if(event.getServiceNum() == 0 && event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车(带目的地号)
				appDataATOCommand = trainRuntaskService.getStationLeaveUnplan(event);
			}
			sender.sendATOCommand(appDataATOCommand);
		}catch (Exception e) {
			MyExceptionUtil.printTrace2logger(e);
			LOG.error("[trace.station.leave] 消息处理出错!");
		}
		watch.stop();
		LOG.info("[trace.station.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	/**
	 * 到站停稳消息处理：根据车次时刻表向客户端发送当前车站的站停时间。
	 * @param in
	 */
	@RabbitListener(queues = "#{queueTraceStationArrive.name}")
	public void receiveTraceStationArrive(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.station.arrive] '" + in + "'");
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runtaskUtils.getMapRuntask(event);
			
			// 向客户端发送站停时间
			AppDataStationTiming appDataStationTiming = null;
					
			if(event.getServiceNum() != 0 && task != null){//计划车
				appDataStationTiming = trainRuntaskService.appDataStationTiming(task, event);
			}		
			
			if(event.getServiceNum() == 0){//非计划车
				appDataStationTiming = trainRuntaskService.appDataStationTimingUnplan(event);
			}
			
			sender.senderAppDataStationTiming(appDataStationTiming);//发送发车倒计时消息
		}catch (Exception e) {
			LOG.error("[trace.station.arrive] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[trace.station.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	
	/**
	 * 离开折返轨消息处理(列车换端时，给尾端发送的AOD命令)：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceReturnLeave.name}")
	public void receiveTraceReturnLeave(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.return.leave] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runtaskUtils.getMapRuntask(event);
			AppDataAVAtoCommand appDataATOCommand = null;
			
			if(event.getServiceNum() != 0 && task != null){//计划车
				appDataATOCommand = trainRuntaskService.getReturnLeave(task, event);
			}
			if(event.getServiceNum() == 0 && event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车(带目的地号)
				appDataATOCommand = trainRuntaskService.getStationLeaveUnplan(event);
			}
			sender.sendATOCommand(appDataATOCommand);
		}catch (Exception e) {
			LOG.error("[trace.return.leave] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[trace.return.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 到达折返轨消息处理(列车换端时，给尾端发送的AOD命令)：根据车次时刻表向VOBC发送命令指定下一个区间运行等级/区间运行时间、当前车站的站停时间。
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceReturnArrive.name}")
	public void receiveTraceReturnArrive(String in) {/*
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.return.arrive] '" + in + "'");
		//doWork(in);
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
		event.setNextStationId(convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
		
		//添加列车到站信息
		//runTaskService.updateMapTrace(event);
		
		//获取或 更新运行图任务信息
		TrainRunTask task = runTaskService.getMapRuntask(event);
		
		// 向该车发送站间运行等级
		AppDataAVAtoCommand appDataATOCommand = null;
		if(event.getServiceNum() != 0 && task != null){//计划车
			appDataATOCommand = runTaskService.aodCmdReturn(event, task);
		}
		if(event.getServiceNum() == 0 && event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车(带目的地号)
			*//** 判断目的地号是否为转换轨,转换轨则发回段命令*//*
			if("ZH".equals(event.getDstCode()) && event.getTrainDir() == 0x55){
				*//**ATO命令信息*//*
				appDataATOCommand = new AppDataAVAtoCommand();
				*//**初始化ATO命令数据为默认值*//*
				appDataATOCommand = runTaskService.initAtoCommand(appDataATOCommand);
				appDataATOCommand.setReserved((int) event.getSrc());	//预留字段填车辆VID
				appDataATOCommand.setCargroupLineNum(event.getCargroupLineNum());
				appDataATOCommand.setCargroupNum(event.getCargroupNum());
				appDataATOCommand.setTrainNum(event.getTrainNum());
				*//**设置目的地号为终点站站台ID*//*
				appDataATOCommand.setDstCode(runTaskService.convertDstCode2Char(event.getDstCode()));
				appDataATOCommand.setPlanDir((short) event.getTrainDir()); // ??? need rungraph supply!
				appDataATOCommand.setBackDepotCmd((short) 0x55);
			}
			else{
				//appDataATOCommand = runTaskService.aodCmdStationLeaveUnplan(event);
			}
		}
		sender.sendATOCommand(appDataATOCommand);
		watch.stop();
		LOG.info("[trace.return.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	*/}
	
	/**
	 * 到达转换轨时，保存列车位置信息
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceTransformArrive.name}")
	public void receiveTraceTransformArrive(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.transform.arrive] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			//获取或 更新运行图任务信息
			TrainRunTask task = runtaskUtils.getMapRuntask(event);
			
			if(event.getServiceNum() != 0 && task != null){//计划车
				if(event.getTrainDir() == 0x55){//回段
					/**ATO命令信息*/
					AppDataAVAtoCommand appDataATOCommand = new AppDataAVAtoCommand();
					/**初始化ATO命令数据为默认值*/
					appDataATOCommand = runtaskUtils.initAtoCommand();
					appDataATOCommand.setNextSkipCmd((short) 0xFF);
					appDataATOCommand.setDetainCmd((short) 0xFF);
					appDataATOCommand.setReserved((int) event.getSrc());	//预留字段填车辆VID
					//appDataATOCommand.setCargroupLineNum(event.getCargroupLineNum());
					appDataATOCommand.setCargroupNum(event.getCargroupNum());
					appDataATOCommand.setTrainNum(event.getTrainNum());
					/**设置目的地号为终点站站台ID*/
					appDataATOCommand.setDstCode(runtaskUtils.convertDstCode2Char(event.getDstCode()));
					appDataATOCommand.setPlanDir((short) event.getTrainDir()); // ??? need rungraph supply!
					appDataATOCommand.setBackDepotCmd((short) 0x55);
					sender.sendATOCommand(appDataATOCommand);
				}
				/*else{
					TrainRunInfo trainRunInfo = new TrainRunInfo();
					BeanUtils.copyProperties(task, trainRunInfo);
					AppDataAVAtoCommand appDataATOCommand = runTaskService.aodCmdTransform(event, trainRunInfo);
					sender.sendATOCommand(appDataATOCommand);
				}*/
				
			}
			else if(event.getTrainDir() == 0x55){
				/**ATO命令信息*/
				AppDataAVAtoCommand appDataATOCommand = new AppDataAVAtoCommand();
				/**初始化ATO命令数据为默认值*/
				appDataATOCommand = runtaskUtils.initAtoCommand();
				appDataATOCommand.setNextSkipCmd((short) 0xFF);
				appDataATOCommand.setDetainCmd((short) 0xFF);
				appDataATOCommand.setReserved((int) event.getSrc());	//预留字段填车辆VID
				//appDataATOCommand.setCargroupLineNum(event.getCargroupLineNum());
				appDataATOCommand.setCargroupNum(event.getCargroupNum());
				appDataATOCommand.setTrainNum(event.getTrainNum());
				/**设置目的地号为终点站站台ID*/
				appDataATOCommand.setDstCode(runtaskUtils.convertDstCode2Char(event.getDstCode()));
				appDataATOCommand.setPlanDir((short) event.getTrainDir()); // ??? need rungraph supply!
				appDataATOCommand.setBackDepotCmd((short) 0xFF);
				sender.sendATOCommand(appDataATOCommand);
			}
			//--------------------
		}catch (Exception e) {
			LOG.error("[trace.transform.arrive] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[trace.transform.arrive] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 到达转换轨时，保存列车位置信息
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueTraceTransformLeave.name}")
	public void receiveTraceTransformLeave(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[trace.transform.leave] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainEventPosition event = objMapper.readValue(in, TrainEventPosition.class);
			event.setNextStationId(runtaskUtils.convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
			
			if(event.getServiceNum() != 0 && event.getTrainDir() == 0xAA){//出段
				//获取或 更新运行图任务信息
				TrainRunTask task = runtaskUtils.getMapRuntask(event);
				if(task != null){
					TrainRunInfo trainRunInfo = new TrainRunInfo();
					BeanUtils.copyProperties(task, trainRunInfo);
					AppDataAVAtoCommand appDataATOCommand = trainRuntaskService.getTransformLeave(task, event);
					sender.sendATOCommand(appDataATOCommand);
				
				}
			}
		}catch (Exception e) {
			LOG.error("[trace.transform.leave] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[trace.transform.leave] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
}
