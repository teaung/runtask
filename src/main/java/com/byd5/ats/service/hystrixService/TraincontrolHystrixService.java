package com.byd5.ats.service.hystrixService;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskConstant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		return null;
	}
	
	
	/**
	 * 获取当前站台停站时间
	 */
	@HystrixCommand(fallbackMethod = "fallbackGetDwellTime",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public Integer getDwellTime(Integer platformId){
		String dwelltimeStr = restTemplate.getForObject(RuntaskConstant.HX_CONTROL_DWELL_TIME, String.class, platformId);
		logger.info("[getDwellTime] platformId:{} defDwellTime:{}", platformId, dwelltimeStr);
		if(dwelltimeStr != null){
			try{
				return Integer.parseInt(dwelltimeStr);
			}catch (Exception e) {
				sender.senderAlarmEvent("ATO命令下发失败,系统停站时间参数含非法字符");
				return null;
			}
		}
		else{
			sender.senderAlarmEvent("从serv35-traincontrol未获取到站台"+platformId+"停站时间");
		}
		return null;	
	}
	
	public Integer fallbackGetDwellTime(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"停站时间失败，运行控制服务故障!");
		return null;
	}
	
	
	/**
	 * 获取当前站台默认停站时间
	 */
	@HystrixCommand(fallbackMethod = "getAllTrainStatusFallback",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public List<TrainEventPosition> getAllTrainStatus() {
		List<TrainEventPosition> alltrain = null;
		String allTrainStatus = restTemplate.getForObject(RuntaskConstant.HX_TRACE_CARS, String.class);
		logger.info("[getAllTrainStatus] allTrainStatus: " + allTrainStatus);
		try {
			if(allTrainStatus != null){
				ObjectMapper mapper = new ObjectMapper();
				alltrain = mapper.readValue(allTrainStatus, new TypeReference<List<TrainEventPosition>>() {});
			}
		} catch (Exception e) {
			logger.error("[getAllTrainStatus] 获取所有列车位置信息解析出错！");
			MyExceptionUtil.printTrace2logger(e);
			alltrain = null;
		}
		
		return alltrain;	
	}
	public List<TrainEventPosition> getAllTrainStatusFallback(){
		sender.senderAlarmEvent("获取正线所有列车位置信息，识别跟踪服务故障!");
		return null;
	}
	
	/**
	 * 获取当前站台的下一站站台ID
	 */
	@HystrixCommand(fallbackMethod = "getNextPlatformIdFallback")
	public Integer getNextPlatformId(short trainDir, Integer platform) {
		Integer nextPlatformId = null;
		String nextPlatformIdStr = restTemplate.getForObject(RuntaskConstant.HX_TRACE_NEXTPLATFORM, String.class, trainDir, platform);
		logger.info("[getNextPlatformId] nextPlatformId: " + nextPlatformIdStr);
		try{
			nextPlatformId = Integer.parseInt(nextPlatformIdStr);
		} catch (Exception e) {
			MyExceptionUtil.printTrace2logger(e);
			nextPlatformId = null;
		}
		return nextPlatformId;	
	}
	public Integer getNextPlatformIdFallback(short trainDir, Integer platform){
		sender.senderAlarmEvent("获取当前站台的下一站站台ID，识别跟踪服务故障!");
		return null;
	}
	
	/**
	 * 获取当前站台默认停站时间
	 */
	@HystrixCommand(fallbackMethod = "getDefDwellTimeFallback")
	public Integer getDefDwellTime(Integer platformId){
		Integer defDwellTime = null;
		String json = restTemplate.getForObject(RuntaskConstant.HX_PARA_TIME, String.class, "116");
		logger.info("[getDefDwellTime] defDwellTime:{}", json);
		if(json != null){
			try{
				ObjectMapper mapper = new ObjectMapper();
				Map mapjson = mapper.readValue(json, Map.class); // json转换成map
				logger.info("[getDefDwellTime] platformId:{} defDwellTime:{}", platformId, mapjson.get("tepValue"));
				defDwellTime = (Integer) mapjson.get("tepValue");
			}catch (Exception e) {
				sender.senderAlarmEvent("ATO命令下发失败,系统停站时间参数含非法字符");
				defDwellTime = null;
			}
		}
		else{
			sender.senderAlarmEvent("从serv50-maintenance未获取到站台"+platformId+"停站时间");
		}
		return defDwellTime;	
	}
	public Integer getDefDwellTimeFallback(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"默认停站时间失败，参数管理服务故障!");
		return null;
	}

	/**
	 * 获取当前站台默认区间运行时间
	 */
	@HystrixCommand(fallbackMethod = "getDefRunTimeFallback")
	public Integer getDefRunTime(Integer platformId) {
		Integer runtime = null;
		String json = restTemplate.getForObject(RuntaskConstant.HX_PARA_TIME, String.class, getRunTimeStr(platformId));
		logger.info("[getDefRunTime] defRunTime:{}", json);
		if(json != null){
			try{
				ObjectMapper mapper = new ObjectMapper();
				Map mapjson = mapper.readValue(json, Map.class); // json转换成map
				logger.info("[getDefRunTime] platformId:{} defRunTime:{}", platformId, mapjson.get("tepValue"));
				runtime = (Integer) mapjson.get("tepValue");
			}catch (Exception e) {
				sender.senderAlarmEvent("ATO命令下发失败,系统区间运行时间参数含非法字符");
				logger.error("[getDefRunTime] ATO命令下发失败,系统区间运行时间参数含非法字符!");
				runtime = null;
			}
		}
		else{
			sender.senderAlarmEvent("从serv50-maintenance未获取到站台"+platformId+"区间运行时间");
		}
		return runtime;	
	}
	public Integer getDefRunTimeFallback(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"默认区间时间失败，参数管理服务故障!");
		return null;
	}
	
	public String getRunTimeStr(Integer platformId) {
		String runTimeStr = null;
		switch (platformId) {
		default:
			System.out.println("打印默认值");
			break;
		case 8:
			runTimeStr = "eight2one";
			break;
		case 1:
			runTimeStr =  "one2two";
			break;
		case 2:
			runTimeStr =  "two2three";
			break;
		case 3:
			runTimeStr =  "three2four";
			break;
		case 4:
			runTimeStr =  "four2five";
			break;
		case 5:
			runTimeStr =  "five2six";
			break;
		case 9:
			runTimeStr =  "returnTrack2seven";
			break;
		case 7:
			runTimeStr =  "seven2eight";
			break;
		case 6:
			runTimeStr =  "six2returnTrack";
			break;
		case 0:
			runTimeStr =  "returnTrack2transform";
			break;
		}
		logger.info("[getRunTimeStr] runTimeStr: " + runTimeStr);
		return runTimeStr;
	}
}
