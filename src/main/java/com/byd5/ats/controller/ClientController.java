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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataDepartCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.BackDepart2AppData;
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
				Map mapData = new HashMap();
				
				//---------------停站时间列表为空，则查询数据库获取--------------
				if(runTaskHandler.mapDwellTime.size() == 0){
					//mapData.put("messageType", "getRuntaskAllCommand");
					String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
					mapData = new HashMap<String, Object>();
					List<AppDataDwellTimeCommand> dataList = mapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
					//List<AppDataDwellTimeCommand> dataList = (List<AppDataDwellTimeCommand>) mapData.get("commandData");
					for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
						runTaskHandler.mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
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
					LOG.error("[appDataDepartCommand] save error Or parse error." );
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
			
			if(runtaskCmdType == 104){//立即发车
				AppDataDepartCommand departCommand = mapper.readValue(json, AppDataDepartCommand.class);
				// 检查该车是否有记录
				Integer platformId = departCommand.getPlatformId();
				Integer carNum = departCommand.getGroupNum();
				TrainRunTask task = null;
				if (runTaskHandler.mapRunTask.containsKey(carNum)) {
					task = runTaskHandler.mapRunTask.get(carNum);
				}
				TrainEventPosition event = null;
				if (runTaskHandler.mapTrace.containsKey(carNum)) {
					event = runTaskHandler.mapTrace.get(carNum);
				}
				
				// 向该车发送站间运行等级
				AppDataATOCommand appDataATOCommand = null;
				AppDataStationTiming appDataStationTiming = null;

				if (task != null && event != null && event.getStation() == platformId) {
					//-------------------给车发AOD命令(停站时间0)----------------
					appDataATOCommand = runTaskHandler.appDataATOCommandEnter(task, event);
					appDataATOCommand.setStationStopTime(0x0001);//停站时间设为0，即立即发车
			
					LOG.info("[appDataDepartCommand] ATOCommand: next station ["
							+ appDataATOCommand.getNextStationId() + "] section run time ["
							+ appDataATOCommand.getSectionRunLevel()+ "s]"
							+ "section stop time ["+ appDataATOCommand.getStationStopTime()
							+ "s]");
					sender.sendATOCommand(appDataATOCommand);
					
					//-------------------给客户端发停站时间0----------------
					appDataStationTiming = runTaskHandler.appDataStationTiming(task, event);
					appDataStationTiming.setTime(0x0001);
					
					LOG.info("[appDataDepartCommand] AppDataTimeStationStop: this station ["
							+ appDataStationTiming.getStation_id() + "] section stop time ["
							+ appDataStationTiming.getTime()
							+ "s]");
					sender.senderAppDataStationTiming(appDataStationTiming);
					
					BackDepart2AppData BackDepart2AppData = new BackDepart2AppData(runtaskCmdType, true, "设置成功", platformId);
					map.put("tgi_msg", BackDepart2AppData);
					result = mapper.writeValueAsString(map);
				}
				else {
					LOG.info("[appDataDepartCommand] not find the car (" + carNum + ") in runTask list, so do nothing.");
					BackDepart2AppData BackDepart2AppData = new BackDepart2AppData(runtaskCmdType, false, "设置失败", platformId);
					map.put("tgi_msg", BackDepart2AppData);
					result = mapper.writeValueAsString(map);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("[dwellTimeOrDepartCommand] parse data error.");
			e.printStackTrace();
		}
		
		LOG.info("--sender--" + result);
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
			String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
			List<AppDataDwellTimeCommand> dataList = mapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map

			for(AppDataDwellTimeCommand dwellTimeCommand:dataList){
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
}
