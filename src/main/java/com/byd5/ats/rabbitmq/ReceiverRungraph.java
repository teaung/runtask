package com.byd5.ats.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收运行图的消息
 * 
 */
public class ReceiverRungraph {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverRungraph.class);
	
	@Autowired
	private RuntaskUtils runtaskUtils;

	@RabbitListener(queues = "#{queueRungraph.name}")
	public void receiveRungraph(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[rungraph] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainRunTask task = objMapper.readValue(in, TrainRunTask.class);
			// 添加运行任务列表
			Integer carNum = task.getTraingroupnum();
			runtaskUtils.updateMapRuntask(carNum, task);
			
		}catch (Exception e) {
			LOG.error("[rungraph runtask] parse data error!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		
		watch.stop();
		System.out.println("[rungraph] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 列车出入段时，表号、车次号、车组号信息
	 * @param in
	 * @throws Exception
	 */
	@RabbitListener(queues = "#{queueRungraphRunInfo.name}")
	public void receiveRungraphRunInfo(String in){
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[rungraph RunInfo] '" + in + "'");
		
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		watch.stop();
		LOG.info("[rungraph RunInfo] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/**
	 * 当列车车次号变更时，收到运行图发来的新车次时刻表后，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param in
	 * @throws Exception 
	 */
	@RabbitListener(queues = "#{queueRungraphChangeTask.name}")
	public void receiveRungraphChangeTask(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[rungraph.changeTask] '" + in + "'");
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			TrainRunTask task = objMapper.readValue(in, TrainRunTask.class);
			if(task != null){
				runtaskUtils.updateMapRuntask(task.getTraingroupnum(), task);
			}
			
		}catch (Exception e) {
			LOG.error("[rungraph.changeTask] 消息处理出错!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		watch.stop();
		LOG.info("[rungraph.changeTask] Done in " + watch.getTotalTimeSeconds() + "s");
	}
}
