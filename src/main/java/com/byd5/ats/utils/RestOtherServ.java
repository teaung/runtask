package com.byd5.ats.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


@Component
public class RestOtherServ {
	private static final Logger LOG = LoggerFactory.getLogger(RestOtherServ.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	
	/**
	 * 获取运行图服务的运行任务
	 * @param carNum 车组号
	 * @param tablenum 表号
	 * @param trainnum 车次号
	 * @return 
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*@HystrixCommand(fallbackMethod = "getRuntaskError")
	public String getRuntask(Integer carNum, short tablenum, short trainnum) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objMapper = new ObjectMapper();
		String resultMsg = null;
		resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, carNum, tablenum, trainnum);
		try{
			if(resultMsg != null){
				return resultMsg;
				TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
				if(newtask != null){
					mapRunTask.put(carNum, newtask);
				}else{
					//需要发报警信息
					LOG.error("get runtask error. runtask not found");
				}
			}
			else{
				LOG.error("getRuntask fail, or getRuntask is null!");
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("fallback data parse error!");
		}
		return resultMsg;
		
	}*/
	
	/**
	 * 获取列车控制服务的站台跳停状态
	 * @param platformId 站台ID
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*@HystrixCommand(fallbackMethod = "connectTraincontrolError")
	public Integer getSkipStationStatus(Integer platformId) throws JsonParseException, JsonMappingException, IOException{
		String skipStatusStr = null;
		skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
		
		Integer skipStatus = Integer.getInteger(skipStatusStr);
		return skipStatus;
	}*/
	
	/**
	 * 获取运行图服务的所有站台停站时间
	 * @return 
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@HystrixCommand(fallbackMethod = "getDwellTimeListError")
	public String getDwellTimeList() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objMapper = new ObjectMapper();
		String resultMsg = null;
		List<AppDataDwellTimeCommand> dataList = new ArrayList<AppDataDwellTimeCommand>();
		
		resultMsg = restTemplate.getForObject("http://serv-trainrungraph/server/getRuntaskAllCommand", String.class);
		try{
			if(resultMsg != null){
				return resultMsg;
				/*dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
				for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
					mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
				}*/
			}else{
				LOG.error("getRuntaskAllCommand fail, or getRuntaskAllCommand is null!");
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("fallback data parse error!");
			e.printStackTrace();
		}
		return resultMsg;	
		
	}
	
	/**
	 * 保存某个站台停站时间至运行图服务中
	 * @param dwellTimeCommandJson 设置停站时间命令字符串
	 * @return
	 */
	/*@HystrixCommand(fallbackMethod = "saveRuntaskCommandError")
	public String saveRuntaskCommand(String dwellTimeCommandJson){
		String resultMsg = null;
		resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/saveRuntaskCommand?json={json}", String.class, dwellTimeCommandJson);
		return resultMsg;
	}
	
	public String getRuntaskError() {
		LOG.error("getRuntask---[serv31-trainrungraph] can't connection!---");
		return null;
	}*/
	
	public String getDwellTimeListError() {
		LOG.error("getDwellTimeError---[serv31-trainrungraph] can't connection!---");
		return null;
	}
	
	public Integer connectTraincontrolError() {
		LOG.error("---[serv35-traincontrol] can't connection!---");
		return null;
	}
	
	public String saveRuntaskCommandError() {
		LOG.error("saveRuntaskCommand---[serv31-trainrungraph] can't connection!---");
		return null;
	}
}
