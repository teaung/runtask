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
 * 接收运行调整的消息
 * 
 */
public class ReceiverAdjust {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverAdjust.class);
	
	@Autowired
	private RuntaskUtils runtaskUtils;

	@RabbitListener(queues = "#{queueAdjust.name}")
	public void receiveAdjust(String in) {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[receiveAdjust] '" + in + "'");
		
		TrainRunTask adjustTask = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			adjustTask = objMapper.readValue(in, TrainRunTask.class);
			
			// 更新运行任务列表
			Integer carNum = adjustTask.getTraingroupnum();
			runtaskUtils.updateMapRuntask(carNum, adjustTask);
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[receiveAdjust] parse data error!");
			MyExceptionUtil.printTrace2logger(e);
		}
		watch.stop();
		System.out.println("[receiveAdjust] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
}
