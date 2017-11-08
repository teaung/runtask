package com.byd5.ats.service.hystrixService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.utils.RuntaskConstant;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service("TraincontrolHystrixService")
public class TraincontrolHystrixService {

	private static final Logger logger = LoggerFactory.getLogger(TraincontrolHystrixService.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SenderDepart sender;
	
	/**
	 * 获取当前站台跳停状态
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetSkipStationStatus",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getSkipStationStatus(Integer platformId){
		String skipStatusStr = restTemplate.getForObject(RuntaskConstant.HX_CONTROL_SKIP_STATUS, String.class, platformId);
		return skipStatusStr;	
	}
	
	public String fallbackGetSkipStationStatus(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"跳停状态失败，运行控制服务故障!");
		logger.error("[getSkipStationStatus] serv35-traincontrol connetc error!");
		return "error";
	}
	
	
	/**
	 * 获取当前站台停站时间
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetDwellTime",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getDwellTime(Integer platformId){
		String dwelltimeStr = restTemplate.getForObject(RuntaskConstant.HX_CONTROL_DWELL_TIME, String.class, platformId);
		return dwelltimeStr;	
	}
	
	public String fallbackGetDwellTime(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"停站时间失败，运行控制服务故障!");
		logger.error("[getDwellTime] serv35-traincontrol connetc error!");
		return "error";
	}
}
