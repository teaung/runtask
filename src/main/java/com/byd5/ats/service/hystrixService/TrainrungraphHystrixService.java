package com.byd5.ats.service.hystrixService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.utils.DstCodeEnum;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service("TrainrungraphHystrixService")
public class TrainrungraphHystrixService {

	private static final Logger logger = LoggerFactory.getLogger(TrainrungraphHystrixService.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SenderDepart sender;
	
	/**
	 * 获取所有车站站停时间
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetDwellTime")
	public List<AppDataDwellTimeCommand> getDwellTime(){
		List<AppDataDwellTimeCommand> dataList = null;
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_DWELL_ALL, String.class);
		try{
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
		} catch (Exception e) {
			logger.error("[getDwellTime] :"+resultMsg);
			MyExceptionUtil.printTrace2logger(e);
			dataList = null;
		}
		return dataList;	
	}
	
	public List<AppDataDwellTimeCommand> fallbackGetDwellTime(){
		sender.senderAlarmEvent("获取站台停站时间失败，运行图服务故障!");
		logger.error("[getAllStopTime] serv31-trainrungraph can't connect!");
		return null;
	}
	
	
	/**
	 * 保存设置停站时间命令
	 */
	@HystrixCommand(fallbackMethod = "fallbackSaveRuntaskCommand")
	public String saveRuntaskCommand(String commandStr){
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_DWELL_UPDATE, String.class, commandStr);
		return resultMsg;	
	}
	
	public String fallbackSaveRuntaskCommand(String commandStr){
		sender.senderAlarmEvent("设置站台停站时间失败，运行图服务故障!");
		return "error";
	}
	
	
	/**
	 * 获取运行图运行任务
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetRuntask")
	public TrainRunTask getRuntask(TrainEventPosition event){
		TrainRunTask runTask = null;
		Integer platformId = DstCodeEnum.getPlatformIdByPhysicalPt(event.getTrainHeaderAtphysical());
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_TASK
				, String.class, event.getCargroupNum(), event.getServiceNum(), event.getTrainNum(), platformId);
		try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			runTask = objMapper.readValue(resultMsg, TrainRunTask.class);
		} catch (Exception e) {
			MyExceptionUtil.printTrace2logger(e);
			logger.info("[getRuntask] :"+resultMsg);
			sender.senderAlarmEvent("没有找到车组号为:"+event.getCargroupNum()+" 表号为:"+event.getServiceNum()+" 车次号为:"+event.getTrainNum()
			+" 在站台ID为:"+platformId+"的运行任务");
			runTask = null;
		}
		return runTask;	
	}
	
	public TrainRunTask fallbackGetRuntask(TrainEventPosition event){
		sender.senderAlarmEvent("获取列车运行任务失败，运行图服务故障!");
		return null;
	}
	
	
	/*public void senderAlarmEvent(String msg){
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(msg);
		template.convertAndSend(EXCHANGE_RUNGRAPGH, ROUTINGKEY_ALARM_ALERT, alarmEvent.toString());
		logger.error("[x] AlarmEvent: "+alarmEvent);
	}*/
	
	/**
	 * 获取当前站台对应的下一车次时刻表
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetNextRuntask")
	public TrainRunTask getNextRuntask(int groupnum, int tablenum, int trainnum, int platformId){
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_NEXTTASK , String.class, groupnum, tablenum, trainnum, platformId);
		try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return objMapper.readValue(resultMsg, TrainRunTask.class);
		} catch (Exception e) {
			MyExceptionUtil.printTrace2logger(e);
			logger.info("[getNextRuntask] :"+resultMsg);
			sender.senderAlarmEvent("没有找到车组号为:"+groupnum+" 表号为:"+tablenum+" 车次号为:"+trainnum +" 的下一车次在站台ID为:"+platformId+"的运行任务");
			return null;
		}
	}
	
	public TrainRunTask fallbackGetNextRuntask(int groupnum, int tablenum, int trainnum, int platformId){
		sender.senderAlarmEvent("获取列车运行任务失败，运行图服务故障!");
		return null;
	}
}
