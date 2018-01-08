package com.byd5.ats.service.hystrixService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.utils.DstCodeEnum;
import com.byd5.ats.utils.RuntaskConstant;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

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
	@HystrixCommand(fallbackMethod = "fallbackGetDwellTime",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getDwellTime(){
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_DWELL_ALL, String.class);
		return resultMsg;	
	}
	
	public String fallbackGetDwellTime(){
		//sender.senderAlarmEvent("获取站台停站时间失败，运行图服务故障!");
		logger.error("[getAllStopTime] serv31-trainrungraph can't connect!");
		return "error";
	}
	
	
	/**
	 * 保存设置停站时间命令
	 */
	@HystrixCommand(fallbackMethod = "fallbackSaveRuntaskCommand",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String saveRuntaskCommand(String commandStr){
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_DWELL_UPDATE, String.class, commandStr);
		return resultMsg;	
	}
	
	public String fallbackSaveRuntaskCommand(String commandStr){
		sender.senderAlarmEvent("设置站台停站时间失败，运行图服务故障!");
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
	public String getRuntask(TrainEventPosition event){
		Integer platformId = DstCodeEnum.getPlatformIdByPhysicalPt(event.getTrainHeaderAtphysical());
//		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_TASK
//				, String.class, event.getCargroupNum(), event.getServiceNum(), event.getTrainNum(), event.getNextStationId());
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_TASK
				, String.class, event.getCargroupNum(), event.getServiceNum(), event.getTrainNum(), platformId);
		if(resultMsg == null || resultMsg.equals("null")){
			sender.senderAlarmEvent("没有找到车组号为:"+event.getCargroupNum()+" 表号为:"+event.getServiceNum()+" 车次号为:"+event.getTrainNum()
			+" 在站台ID为:"+platformId+"的运行任务");
			return null;
		}
		return resultMsg;	
	}
	
	public String fallbackGetRuntask(TrainEventPosition event){
		sender.senderAlarmEvent("获取列车运行任务失败，运行图服务故障!");
		logger.error("[getRuntask] serv31-trainrungraph connetc error!");
		return "error";
	}
	
	
	/*public void senderAlarmEvent(String msg){
		ATSAlarmEvent alarmEvent = new ATSAlarmEvent(msg);
		template.convertAndSend(EXCHANGE_RUNGRAPGH, ROUTINGKEY_ALARM_ALERT, alarmEvent.toString());
		logger.error("[x] AlarmEvent: "+alarmEvent);
	}*/
	
	/**
	 * 获取当前车次终点站的下一车次起点站的离站时间
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetNextRuntask",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getNextRuntask(int groupnum, int tablenum, int trainnum, int platformId){
		String resultMsg = restTemplate.getForObject(RuntaskConstant.HX_RUNGRAPH_NEXTTASK
				, String.class, groupnum, tablenum, trainnum, platformId);
		if(resultMsg == null || resultMsg.equals("null")){
			sender.senderAlarmEvent("没有找到车组号为:"+groupnum+" 表号为:"+tablenum+" 车次号为:"+trainnum
			+" 的下一车次在站台ID为:"+platformId+"的运行任务");
			return null;
		}
		return resultMsg;	
	}
	
	public String fallbackGetNextRuntask(int groupnum, int tablenum, int trainnum, int platformId){
		sender.senderAlarmEvent("获取列车运行任务失败，运行图服务故障!");
		logger.error("[getRuntask] serv31-trainrungraph connetc error!");
		return "error";
	}
}
