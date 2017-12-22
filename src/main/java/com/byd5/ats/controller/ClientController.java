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

import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.BackDwellTime2AppData;
import com.byd5.ats.message.DwellTimeData;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.service.RunTaskService;
import com.byd5.ats.service.hystrixService.TraincontrolHystrixService;
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
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
	private TrainrungraphHystrixService trainrungraphHystrixService;
	@Autowired
	private TraincontrolHystrixService traincontrolHystrixService;
	
	/*** 处理客户端请求 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException */
	@RequestMapping(value = "/client")
	public @ResponseBody String setDwellTime(@RequestParam String json) throws JsonParseException, JsonMappingException, IOException{
		String result = null;
		ObjectMapper mapper = new ObjectMapper();
		BackDwellTime2AppData BackDwellTime2AppData = null;
		//反序列化
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		/*AppDataDwellTimeCommand dwellTimeCommand = new AppDataDwellTimeCommand();
		dwellTimeCommand.setPlatformId(1);
		dwellTimeCommand.setSetWay(1);
		dwellTimeCommand.setRuntaskCmdType((short) 114);
		dwellTimeCommand.setTime(50);
		Integer runtaskCmdType = 114;*/
		
		LOG.info("--receive--"+json);
		//try {
			
			Map mapjson = new HashMap();
			mapjson = mapper.readValue(json, Map.class); // json转换成map
			Integer runtaskCmdType = (Integer) mapjson.get("runtaskCmdType");
			
			if(runtaskCmdType == 114){//停站时间
				AppDataDwellTimeCommand dwellTimeCommand = null;
				try{
					dwellTimeCommand = mapper.readValue(json, AppDataDwellTimeCommand.class);
				}catch (Exception e) {
					// TODO: handle exception
					BackDwellTime2AppData = new BackDwellTime2AppData(runtaskCmdType, false, "设置失败，消息格式有误", 0, 0, 0);
					result = "{\"tgi_msg\":"+mapper.writeValueAsString(BackDwellTime2AppData)+"}";
					LOG.info("[setDwellTime]--sender--" + result);
					return result;
				}
				Integer platform = dwellTimeCommand.getPlatformId();//站台ID
				
				//---------------停站时间列表为空，则查询数据库获取--------------
				runTaskHandler.getmapDwellTime();
				
				if (!runTaskHandler.mapDwellTime.containsKey(platform)) {
					runTaskHandler.mapDwellTime.put(platform, dwellTimeCommand);
				}
				else {
					Integer id = runTaskHandler.mapDwellTime.get(platform).getId();
					dwellTimeCommand.setId(id);
					runTaskHandler.mapDwellTime.replace(platform, dwellTimeCommand);
				}
				
				//----------------更新数据库停站时间命令，并更新map列表------------------
				String resultMsg = trainrungraphHystrixService.saveRuntaskCommand(mapper.writeValueAsString(dwellTimeCommand));
				if(resultMsg == null || resultMsg.equals("error")){
					LOG.error("[setDwellTime] save error Or parse error." );
					BackDwellTime2AppData = new BackDwellTime2AppData(runtaskCmdType, false, "设置失败，", platform, dwellTimeCommand.getTime(), dwellTimeCommand.getSetWay());

				}else if(resultMsg != null && !resultMsg.equals("error")){
					dwellTimeCommand = mapper.readValue(resultMsg, AppDataDwellTimeCommand.class);
					runTaskHandler.mapDwellTime.replace(platform, dwellTimeCommand);
					BackDwellTime2AppData = new BackDwellTime2AppData(runtaskCmdType, true, "设置成功", platform, dwellTimeCommand.getTime(), dwellTimeCommand.getSetWay());
					
				}
				
				//--------------------返回结果给客户端----------------------------
				result = "{\"tgi_msg\":"+mapper.writeValueAsString(BackDwellTime2AppData)+"}";
			}
			
		/*} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("[setDwellTime] parse data error.");
			e.printStackTrace();
		}*/
		
		LOG.info("[setDwellTime]--sender--" + result);
		return result;
	}

	/*** 处理客户端请求 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException */
	@RequestMapping(value = "/getAllStopTime")
	public @ResponseBody String getRuntaskAllCommand() throws JsonParseException, JsonMappingException, IOException{
		//LOG.info("---[R]--getRuntaskAllCommand--");
		String result = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		List<DwellTimeData> dwellTimeDataList = new ArrayList<DwellTimeData>();
		
		//---------------停站时间列表为空，则查询数据库获取--------------
		runTaskHandler.getmapDwellTime();

		for(AppDataDwellTimeCommand dwellTimeCommand:runTaskHandler.mapDwellTime.values()){
			DwellTimeData dwellTimeData = new DwellTimeData();
			BeanUtils.copyProperties(dwellTimeCommand, dwellTimeData);
			dwellTimeDataList.add(dwellTimeData);
		}	
				
		result = "{\"ats_station_stop_time\":"+mapper.writeValueAsString(dwellTimeDataList)+"}";
		
		//LOG.info("---[S]--getRuntaskAllCommand--"+result);
		return result;
	}
	
	/**
	 * 设置立即发车
	 * @param platformId
	 * @param carNum
	 * @return
	 */
	@RequestMapping(value = "/setDepartCmd")
	public @ResponseBody String departCommand(Integer platformId, Integer carNum) throws JsonParseException, JsonMappingException, IOException{
		String result = "0";//0:失败，1:成功
		LOG.info("---[S]--setDepartCmd--platformId:"+platformId+" carNum:"+carNum);
		try{
			List<TrainEventPosition> carList = getAllTrainStatus(platformId);
			for(TrainEventPosition event:carList){
				if(event.getStation() != null && platformId.equals(event.getStation())){
					TrainRunTask task = null;
					
					if(event != null && event.getServiceNum() != 0){
						task = runTaskHandler.getMapRuntask(event);	
					}
					
					// 向该车发送站间运行等级
					AppDataAVAtoCommand appDataATOCommand = null;
					AppDataStationTiming appDataStationTiming = null;

					if (task != null && event != null && event.getStation() == platformId
							&& event.getServiceNum() != 0) {
						//-------------------给车发AOD命令(停站时间0)----2017-11-28------------
						appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
						
						if(appDataATOCommand == null || appDataATOCommand != null && appDataATOCommand.getDetainCmd() == 0x55){
							result = "0";
							return result;
						}
						//-------------------给客户端发停站时间0----------------
						appDataStationTiming = runTaskHandler.appDataStationTiming(task, event);
						
						appDataATOCommand.setPlatformStopTime(0x0001);//停站时间设为0，即立即发车
						appDataStationTiming.setTime(0x0000);
						
						//---------------发送消息--------------------------
						sender.sendATOCommand(appDataATOCommand);
						sender.senderAppDataStationTiming(appDataStationTiming);
					}
					else if(event != null && event.getServiceNum() == 0){
						//LOG.info("[appDataDepartCommand] not find the car (" + carNum + ") in runTask list, so do nothing.");
						LOG.info("[setDepartCmd] -------------unplanTrain-----------");
						//---------------20171212----给车发AOD命令(停站时间0)----------------
						appDataATOCommand = runTaskHandler.getStationArriveUnplan(event);//非计划车
						if(appDataATOCommand == null || appDataATOCommand != null && appDataATOCommand.getDetainCmd() == 0x55){//有扣车，不能下发立即发车，并发告警
							sender.senderAlarmEvent("当前不满足执行立即发车的条件");
							result = "0";
							return result;
						}
						appDataATOCommand.setPlatformStopTime(0x0001);//停站时间设为0，即立即发车
						sender.sendATOCommand(appDataATOCommand);
						
						//-------------------给客户端发停站时间0----------------
						appDataStationTiming = runTaskHandler.appDataStationTimingUnplan(event);
						appDataStationTiming.setTime(0x0000);
						sender.senderAppDataStationTiming(appDataStationTiming);
						
					}
					result = "1";
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("setDepartCmd error!");
			e.printStackTrace();
			result = "0";
		}
		LOG.info("---[S]--setDepartCmd--result:"+result);		
		return result;

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
		//LOG.info("[getAlltrainruntask] sender to PIS data: "+resultMsg);
		return resultMsg;
	}
	
	/**
	 * 更新车站扣车状态信息，给车发扣车命令
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value="/dtStatus", method=RequestMethod.GET)
	public  String dtStatus(String dtStatusStr) throws JsonParseException, JsonMappingException, IOException{
		String resultMsg = null;
		try{
			ObjectMapper mapper = new ObjectMapper();
			List<Byte> dtStatus = mapper.readValue(dtStatusStr, new TypeReference<List<Byte>>() {}); // json转换成map
			LOG.info("[dtStatus] dtStatus: " + dtStatus);
			List<Byte> listDtStatus = runTaskHandler.listDtStatus;
			//Map<Integer, TrainEventPosition> mapTrace = runTaskHandler.mapTrace;
			for(int i=-0; i<dtStatus.size(); i++){
				if(dtStatus.get(i) != listDtStatus.get(i)){//扣车状态有改变
					listDtStatus.set(i, dtStatus.get(i));//更新该车站扣车状态
					Integer platformId = i + 1;
					List<TrainEventPosition> carList = getAllTrainStatus(platformId);
					for(TrainEventPosition event:carList){
						//获取或 更新运行图任务信息
						TrainRunTask task = runTaskHandler.getMapRuntask(event);
						
						// 向该车发送站间运行等级
						AppDataAVAtoCommand appDataATOCommand = null;
						if(event.getServiceNum() != 0 && task != null && event.getServiceNum() != 0){
//							appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
							if(event.getStation() != null){//计划车(在站台上时才下发ATO)
								appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
							}
							
						}
						else if(event.getServiceNum() == 0){//非计划车
							//-------------------给车发扣车AOD命令----------------
							if(event.getStation() != null && event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车
								appDataATOCommand = runTaskHandler.getStationArriveUnplan(event);
//								appDataATOCommand = runTaskHandler.getStationDetainUnplan(event, event.getStation());
							}
							else if(event.getStation() != null && 
									(event.getDstCode() == null || event.getDstCode() != null && "".equals(event.getDstCode()))){//人工车
								appDataATOCommand = runTaskHandler.getStationDetainUnplan(event, event.getStation());
							}
						}
						sender.sendATOCommand(appDataATOCommand);
						//列车设置扣车，给HIM发发车倒计时为0
						/*if(appDataATOCommand != null && appDataATOCommand.getDetainCmd() == 0x55){
							//-------------------给客户端发停站时间0----------------
							AppDataStationTiming appDataStationTiming = runTaskHandler.appDataStationTimingUnplan(event);
							if(appDataStationTiming != null){
								appDataStationTiming.setTime(0x0000);
								sender.senderAppDataStationTiming(appDataStationTiming);
							}
						}*/
						LOG.info("[dtStatus] 设置取消扣车，发送ATO命令 ");
					}
				}
			}
			
			resultMsg = "0";//成功
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[dtStatus] Exception!");
			resultMsg = "1";//失败
			e.printStackTrace();
		}
		LOG.info("[dtStatus] end");
		return resultMsg;
	}
	
	/**
	 * 更新车站扣车状态信息，给车发扣车命令
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value="/sendSkipCmd", method=RequestMethod.GET)
	public  String sendSkipCmd(Integer platformId) throws JsonParseException, JsonMappingException, IOException{
		String resultMsg = null;
		try{
			LOG.info("[sendSkipCmd] platformId: " + platformId);
			
			//列车在区间运行时设置跳停
			List<TrainEventPosition> carList = getAllTrainStatus(platformId);
			for(TrainEventPosition event:carList){
				if(event.getDstCode() != null && !"".equals(event.getDstCode())){//有目的地号
					sendSkipCmd2vobc(event);
				}
			}
		
			resultMsg = "0";//成功
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[sendSkipCmd] Exception!");
			resultMsg = "1";//失败
			e.printStackTrace();
		}
		LOG.info("[sendSkipCmd] end");
		return resultMsg;
	}

	private void sendSkipCmd2vobc(TrainEventPosition event) throws JsonProcessingException {
		//获取或 更新运行图任务信息
		TrainRunTask task = runTaskHandler.getMapRuntask(event);
		
		// 向该车发送站间运行等级
		AppDataAVAtoCommand appDataATOCommand = null;
		if(event.getServiceNum() != 0 && task != null && event.getServiceNum() != 0){//计划车
//			appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
			if(event.getStation() != null){
				appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
			}
			else{
				appDataATOCommand = runTaskHandler.aodCmdStationLeave(task, event);
			}
		}
		else if(event.getServiceNum() == 0){//非计划车
			if(event.getStation() != null){
				//appDataATOCommand = runTaskHandler.getStationArriveUnplan(event);
//				appDataATOCommand = runTaskHandler.getStationSkipUnplan(event, event.getStation());
			}
			else{
				appDataATOCommand = runTaskHandler.getStationSectionUnplan(event);
//				appDataATOCommand = runTaskHandler.getStationSkipUnplan(event, event.getNextStationId());
			}
		}
		sender.sendATOCommand(appDataATOCommand);
		LOG.info("[sendSkipCmd] 站台设置或取消跳停，发送ATO命令 ");
	}
	
	public List<TrainEventPosition> getAllTrainStatus(Integer platformId) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		List<TrainEventPosition> carList = new ArrayList<TrainEventPosition>();
		String alltrainStatusStr = traincontrolHystrixService.getAllTrainStatus();
		if(alltrainStatusStr != null && !alltrainStatusStr.equals("error")){
			List<TrainEventPosition> alltrainStatus = mapper.readValue(alltrainStatusStr, new TypeReference<List<TrainEventPosition>>() {}); // json转换成map
			for(TrainEventPosition event:alltrainStatus){
				if((platformId.equals(event.getNextStationId()) || platformId.equals(event.getStation()))){//头码车(带目的地号)
						//&& event.getDstCode() != null && !"".equals(event.getDstCode())){//头码车(带目的地号)
					event.setNextStationId(convertNextPlatformId(event.getNextStationId()));//转换下一站台ID
					carList.add(event);
				}
			}
		}
		
		LOG.info("[getAllTrainStatus] " + carList);
		return carList;
	}
	
	@RequestMapping(value="/test1", method=RequestMethod.GET)
	public @ResponseBody String trainrungraph() throws JsonParseException, JsonMappingException, IOException{
		String resultMsg = null;
		ObjectMapper mapper = new ObjectMapper();
		try{
			TrainEventPosition event = new TrainEventPosition();
			event.setCargroupNum((short) 103);
			event.setServiceNum((short) 1);
			event.setTrainNum((short) 102);
			TrainRunTask newtask = runTaskHandler.getNewRuntask(event);
			resultMsg = mapper.writeValueAsString(newtask);
			
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[getRuntask] runtask parse error!");
			e.printStackTrace();
		}
		return resultMsg;
	}
	
	private Integer convertNextPlatformId(Integer nextPlatformId){
		if(nextPlatformId == 10){//下一站转换轨
			nextPlatformId = 0;
		}
		
		if(nextPlatformId == 9){//下一站折返轨
			nextPlatformId = 9;
		}
		return nextPlatformId;
	}
}
