package com.byd5.ats.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.BackDwellTime2AppData;
import com.byd5.ats.message.DwellTimeData;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.service.RunTaskService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Component
public class ClientController{

	private static final Logger LOG = LoggerFactory.getLogger(ClientController.class);

	@Autowired
	private RunTaskService runTaskHandler;
	@Autowired
	private SenderDepart sender;
	@Autowired
	private RestTemplate restTemplate;
	
	/*** 处理客户端请求 */
	@RequestMapping(value = "/client")
	public @ResponseBody String client(@RequestParam String json){
		String result = null;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		LOG.info("--receive--"+json);
		
		try {
			
			Map mapjson = new HashMap();
			mapjson = mapper.readValue(json, Map.class); // json转换成map
			Integer runtaskCmdType = (Integer) mapjson.get("runtaskCmdType");
			
			if(runtaskCmdType == 114){//停站时间
				AppDataDwellTimeCommand dwellTimeCommand = mapper.readValue(json, AppDataDwellTimeCommand.class);
				Integer platform = dwellTimeCommand.getPlatformId();//站台ID
				
				//---------------停站时间列表为空，则查询数据库获取--------------
				if(runTaskHandler.mapDwellTime.size() == 0){
					try{
						String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
						if(resultMsg != null){
							List<AppDataDwellTimeCommand> dataList = mapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
							for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
								runTaskHandler.mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
							}
						}else{
							LOG.error("[setDwellTime] serv31-trainrungraph fallback runtask is null!");
						}
						
					}catch (Exception e) {
						// TODO: handle exception
						LOG.error("[setDwellTime] serv31-trainrungraph can't connetc, or runtask parse error!");
						e.printStackTrace();
					}
				}
				
				if (!runTaskHandler.mapDwellTime.containsKey(platform)) {
					runTaskHandler.mapDwellTime.put(platform, dwellTimeCommand);
				}
				else {
					Integer id = runTaskHandler.mapDwellTime.get(platform).getId();
					dwellTimeCommand.setId(id);
					runTaskHandler.mapDwellTime.replace(platform, dwellTimeCommand);
				}
				
				//----------------更新数据库停站时间命令，并更新map列表------------------
				String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/saveRuntaskCommand?json={json}", String.class, mapper.writeValueAsString(dwellTimeCommand));
				if(resultMsg == null){
					LOG.error("[setDwellTime] save error Or parse error." );
				}else{
					dwellTimeCommand = mapper.readValue(resultMsg, AppDataDwellTimeCommand.class);
					//dwellTimeCommand = (AppDataDwellTimeCommand) mapData.get("commandData");
					runTaskHandler.mapDwellTime.replace(platform, dwellTimeCommand);
				}
				
				//--------------------返回结果给客户端----------------------------
				BackDwellTime2AppData BackDwellTime2AppData = new BackDwellTime2AppData(runtaskCmdType, true, "设置成功", platform, dwellTimeCommand.getTime(), dwellTimeCommand.getSetWay());
				map = new HashMap<String, Object>();
				map.put("tgi_msg", BackDwellTime2AppData);
				result = mapper.writeValueAsString(map);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("[setDwellTime] parse data error.");
			e.printStackTrace();
		}
		
		LOG.info("[setDwellTime]--sender--" + result);
		return result;
	}

	/*** 处理客户端请求 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException */
	@RequestMapping(value = "/getAllStopTime")
	public @ResponseBody String getRuntaskAllCommand() throws JsonParseException, JsonMappingException, IOException{
		String result = null;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapData = new HashMap<String, Object>();
		
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//LOG.info("---[R]--getRuntaskAllCommand--");
		List<DwellTimeData> dwellTimeDataList = new ArrayList<DwellTimeData>();
		try{
			if(runTaskHandler.mapDwellTime == null || runTaskHandler.mapDwellTime.size() == 0){
				//runTaskHandler.mapDwellTime = new HashMap<Integer, AppDataDwellTimeCommand>();
				System.out.println("----------------");
				String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
				try{
					if(resultMsg != null){
						List<AppDataDwellTimeCommand> dataList = mapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
						for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
							runTaskHandler.mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
						}
					}else{
						LOG.error("getRuntaskAllCommand fail, or getRuntaskAllCommand is null!");
					}
				}catch (Exception e) {
					// TODO: handle exception
					LOG.error("fallback data parse error!");
					e.printStackTrace();
				}
			}
			//String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
			//List<AppDataDwellTimeCommand> dataList = mapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map

			for(AppDataDwellTimeCommand dwellTimeCommand:runTaskHandler.mapDwellTime.values()){
				DwellTimeData dwellTimeData = new DwellTimeData();
				BeanUtils.copyProperties(dwellTimeCommand, dwellTimeData);
				dwellTimeDataList.add(dwellTimeData);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOG.error("[getRuntaskAllCommand] parse data error.");
		}
				
		mapData.put("ats_station_stop_time", dwellTimeDataList);
		result = mapper.writeValueAsString(mapData);
		
		//LOG.info("---[S]--getRuntaskAllCommand--"+result);
		return result;

	}
	
	/**
	 * 设置立即发车
	 * @param platformId
	 * @param carNum
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/setSkipStationCommand")
	public @ResponseBody String departCommand(Integer platformId, Integer carNum) throws JsonParseException, JsonMappingException, IOException{
		String result = "0";
		LOG.info("---[S]--setDepartCommand--platformId:"+platformId+" carNum:"+carNum);
		try{
			// 检查该车是否有记录
			TrainEventPosition event = null;
			if (runTaskHandler.mapTrace.containsKey(carNum)) {
				event = runTaskHandler.mapTrace.get(carNum);
			}
			
			if(event != null && runTaskHandler.mapRunTask.size() == 0){
				ObjectMapper objMapper = new ObjectMapper();
				String resultMsg = null;
				try{
					resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, carNum, event.getServiceNum(), event.getTrainNum());
					try{
						if(resultMsg != null){
							TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
							if(newtask != null){
								runTaskHandler.mapRunTask.put(carNum, newtask);
							}/*else{
								//需要发报警信息
								LOG.error("get runtask error. runtask not found");
							}*/
						}
						else{
							LOG.error("[setDepartCommand] getRuntask fail, or getRuntask is null!");
						}
					}catch (Exception e) {
						// TODO: handle exception
						LOG.error("[setDepartCommand] fallback data parse error!");
						e.printStackTrace();
					}
				}catch (Exception e) {
					// TODO: handle exception
					LOG.error("[serv31-trainrungraph] can't connection!");
					e.printStackTrace();
				}
				
			}
			
			TrainRunTask task = null;
			if (runTaskHandler.mapRunTask.containsKey(carNum)) {
				task = runTaskHandler.mapRunTask.get(carNum);
			}
			
			// 向该车发送站间运行等级
			AppDataATOCommand appDataATOCommand = null;
			AppDataStationTiming appDataStationTiming = null;

			if (task != null && event != null && event.getStation() == platformId) {
				//-------------------给车发AOD命令(停站时间0)----------------
				appDataATOCommand = runTaskHandler.appDataATOCommandEnter(task, event);
		
				//-------------------给客户端发停站时间0----------------
				appDataStationTiming = runTaskHandler.appDataStationTiming(task, event);
			}
			else {
				//LOG.info("[appDataDepartCommand] not find the car (" + carNum + ") in runTask list, so do nothing.");
				//result = "0";
				LOG.info("[setDepartCommand] -------------unplanTrain-----------");
				
				//-------------------给车发AOD命令(停站时间0)----------------
				appDataATOCommand = runTaskHandler.appDataATOCommandEnterUnplan(event);
		
				//-------------------给客户端发停站时间0----------------
				appDataStationTiming = runTaskHandler.appDataStationTimingUnplan(event);
			}
			
			appDataATOCommand.setStationStopTime(0x0001);//停站时间设为0，即立即发车
			appDataStationTiming.setTime(0x0001);
			
			//---------------发送消息--------------------------
			LOG.info("[setDepartCommand] ATOCommand: next station ["
					+ appDataATOCommand.getNextStationId() + "] section run time ["
					+ appDataATOCommand.getSectionRunLevel()+ "s]"
					+ "section stop time ["+ appDataATOCommand.getStationStopTime()
					+ "s]");
			sender.sendATOCommand(appDataATOCommand);
			
			LOG.info("[setDepartCommand] AppDataTimeStationStop: this station ["
					+ appDataStationTiming.getStation_id() + "] section stop time ["
					+ appDataStationTiming.getTime()
					+ "s]");
			sender.senderAppDataStationTiming(appDataStationTiming);
			result = "1";
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("setDepartCommand error!");
			e.printStackTrace();
			result = "0";
		}
		LOG.info("---[S]--setDepartCommand--result:"+result);		
		return result;

	}
	
	@RequestMapping(value="/test", method=RequestMethod.GET)
	public @ResponseBody Integer getSkipStationStatus(@RequestParam Integer platformId) throws JsonParseException, JsonMappingException, IOException{
		String skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
		System.out.println("-------skipStatusStr--------"+skipStatusStr);
		Integer skipStatus = Integer.getInteger(skipStatusStr);
		System.out.println("-------skipStatus--------"+skipStatus);
		return skipStatus;
	}
	
	@RequestMapping(value="/test1", method=RequestMethod.GET)
	public @ResponseBody String trainrungraph() throws JsonParseException, JsonMappingException, IOException{
		String resultMsg = null;
		ObjectMapper mapper = new ObjectMapper();
		try{
			resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, 101, 1, 102);
			if(resultMsg != null){
				TrainRunTask newtask = mapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
				runTaskHandler.mapRunTask.put(101, newtask);
				LOG.error("[getRuntask] "+resultMsg);
			}else{
				//需要发报警信息
				LOG.error("[getRuntask] serv31-trainrungraph fallback runtask is null!");
			}
			
			resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, 102, 1, 103);
			if(resultMsg != null){
				TrainRunTask newtask = mapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
				runTaskHandler.mapRunTask.put(102, newtask);
				LOG.error("[getRuntask] "+resultMsg);
			}else{
				//需要发报警信息
				LOG.error("[getRuntask] serv31-trainrungraph fallback runtask is null!");
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[getRuntask] serv31-trainrungraph can't connetc, or runtask parse error!");
			e.printStackTrace();
		}
		return resultMsg;
	}
	
	
	@RequestMapping(value="/getAlltrainruntask", method=RequestMethod.GET)
	public @ResponseBody String getAlltrainruntask() throws JsonParseException, JsonMappingException, IOException{
		String resultMsg = null;
		try{
			List<TrainRunTask> json = new ArrayList<TrainRunTask>();
			ObjectMapper mapper = new ObjectMapper();
			Map<Integer, TrainRunTask> allRuntask = runTaskHandler.mapRunTask;
			for(TrainRunTask TrainRunTask:allRuntask.values()){
				json.add(TrainRunTask);
			}
			resultMsg = mapper.writeValueAsString(json);
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[getAlltrainruntask] Exception!");
			e.printStackTrace();
		}
		LOG.info("[getAlltrainruntask] sender to PS data: "+resultMsg);
		return resultMsg;
	}
}
