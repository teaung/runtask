package com.byd5.ats.service.hystrixService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.utils.RuntaskConstant;
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
	public Integer getDwellTime(Integer platformId){
		String dwelltimeStr = restTemplate.getForObject(RuntaskConstant.HX_CONTROL_DWELL_TIME, String.class, platformId);
		logger.info("[getDwellTime] platformId:{} defDwellTime:{}", platformId, dwelltimeStr);
		if(dwelltimeStr != null){
			try{
				return Integer.parseInt(dwelltimeStr);
			}catch (Exception e) {
				// TODO: handle exception
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
		logger.error("[getDwellTime] serv35-traincontrol connetc error!");
		return null;
	}
	
	
	/**
	 * 获取当前站台默认停站时间
	 */
	@HystrixCommand(fallbackMethod = "getAllTrainStatusFallback",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getAllTrainStatus() {
		// TODO Auto-generated method stub
		String allTrainStatus = restTemplate.getForObject(RuntaskConstant.HX_TRACE_CARS, String.class);
		logger.info("[getAllTrainStatus] allTrainStatus: " + allTrainStatus);
		/*if(defDwellTimeStr == null){
			defDwellTimeStr = RuntaskConstant.DEF_DWELL_TIME.toString();//默认值30
		}*/
		return allTrainStatus;	
	}
	public String getAllTrainStatusFallback(){
		sender.senderAlarmEvent("获取正线所有列车位置信息，识别跟踪服务故障!");
		logger.error("[getAllTrainStatus] serv32-traintrace connetc error!");
		return "error";
	}
	
	/**
	 * 获取当前站台的下一站站台ID
	 */
	@HystrixCommand(fallbackMethod = "getNextPlatformIdFallback",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public String getNextPlatformId(short trainDir, Integer platform) {
		// TODO Auto-generated method stub
		String nextPlatformId = restTemplate.getForObject(RuntaskConstant.HX_TRACE_NEXTPLATFORM, String.class, trainDir, platform);
		logger.info("[getNextPlatformId] nextPlatformId: " + nextPlatformId);
		/*if(defDwellTimeStr == null){
			defDwellTimeStr = RuntaskConstant.DEF_DWELL_TIME.toString();//默认值30
		}*/
		return nextPlatformId;	
	}
	public String getNextPlatformIdFallback(short trainDir, Integer platform){
		sender.senderAlarmEvent("获取当前站台的下一站站台ID，识别跟踪服务故障!");
		logger.error("[getNextPlatformId] serv32-traintrace connetc error!");
		return "error";
	}
	
	/**
	 * 获取当前站台默认停站时间
	 */
	@HystrixCommand(fallbackMethod = "getDefDwellTimeFallback",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public Integer getDefDwellTime(Integer platformId){
		String json = restTemplate.getForObject(RuntaskConstant.HX_PARA_TIME, String.class, "116");
		logger.info("[getDefDwellTime] defDwellTime:{}", json);
		if(json != null){
			try{
				ObjectMapper mapper = new ObjectMapper();
				Map mapjson = mapper.readValue(json, Map.class); // json转换成map
				logger.info("[getDefDwellTime] platformId:{} defDwellTime:{}", platformId, mapjson.get("tepValue"));
				return (Integer) mapjson.get("tepValue");
				//return Integer.parseInt(defDwellTimeStr);
			}catch (Exception e) {
				sender.senderAlarmEvent("ATO命令下发失败,系统停站时间参数含非法字符");
				logger.error("[getDefDwellTime] ATO命令下发失败,系统停站时间参数含非法字符!");
				return null;
			}
		}
		else{
			sender.senderAlarmEvent("从serv50-maintenance未获取到站台"+platformId+"停站时间");
		}
		return null;	
	}
	public Integer getDefDwellTimeFallback(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"默认停站时间失败，参数管理服务故障!");
		logger.error("[getDefDwellTime] serv50-maintenance connetc error!");
		//return "error";
		return null;
	}

	/**
	 * 获取当前站台默认停站时间
	 */
	@HystrixCommand(fallbackMethod = "getDefRunTimeFallback",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="3000")
			})
	public Integer getDefRunTime(Integer platformId) {
		// TODO Auto-generated method stub
		String json = restTemplate.getForObject(RuntaskConstant.HX_PARA_TIME, String.class, getRunTimeStr(platformId));
		logger.info("[getDefRunTime] defRunTime:{}", json);
		if(json != null){
			try{
				ObjectMapper mapper = new ObjectMapper();
				Map mapjson = mapper.readValue(json, Map.class); // json转换成map
				logger.info("[getDefRunTime] platformId:{} defRunTime:{}", platformId, mapjson.get("tepValue"));
				return (Integer) mapjson.get("tepValue");
				//return Integer.parseInt(defRunTimeStr);
			}catch (Exception e) {
				sender.senderAlarmEvent("ATO命令下发失败,系统区间运行时间参数含非法字符");
				logger.error("[getDefRunTime] ATO命令下发失败,系统区间运行时间参数含非法字符!");
				return null;
			}
		}
		else{
			sender.senderAlarmEvent("从serv50-maintenance未获取到站台"+platformId+"区间运行时间");
		}
		return null;	
	}
	public Integer getDefRunTimeFallback(Integer platformId){
		sender.senderAlarmEvent("获取站台"+platformId+"默认区间时间失败，参数管理服务故障!");
		logger.error("[getDefRunTime] serv50-maintenance connetc error!");
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
