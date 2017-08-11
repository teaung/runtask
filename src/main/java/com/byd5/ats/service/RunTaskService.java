package com.byd5.ats.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.controller.ClientController;
import com.byd5.ats.message.AppDataATOCommand;
import com.byd5.ats.message.AppDataDepartCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunInfo;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.byd.ats.protocol.ats_vobc.FrameATOStatus;

/**
 * 列车运行任务处理类
 * @author hehg
 *
 */
@Component
public class RunTaskService {
	private static final Logger LOG = LoggerFactory.getLogger(RunTaskService.class);

	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainRunTask> mapRunTask = new HashMap<Integer, TrainRunTask>();

	/**
	 * 列车ATO状态map：以车组号为key，FrameATOStatus类为value
	 */
	public Map<Integer, FrameATOStatus> mapATOStatus = new HashMap<Integer, FrameATOStatus>();
	
	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainEventPosition> mapTrace = new HashMap<Integer, TrainEventPosition>();
	
	/**
	 * 设置停站时间map：以站台ID为key，AppDataDwellTimeOrDepartCommand类为value
	 */
	public Map<Integer, AppDataDwellTimeCommand> mapDwellTime = new HashMap<Integer, AppDataDwellTimeCommand>();
	
	/**
	 * 设置立即发车map：以车组号为key，AppDataDwellTimeOrDepartCommand类为value
	 */
	public Map<Integer, AppDataDepartCommand> mapDepart = new HashMap<Integer, AppDataDepartCommand>();
	
	/**
	 * 当列车到达折返时，收到运行图发来的车次时刻表后，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param task
	 * @return
	 */
	public AppDataATOCommand appDataATOCommandTask(TrainRunTask task) throws Exception {
		AppDataATOCommand cmd = new AppDataATOCommand();

		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setNextZcId(0);//0xffffffff
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCarLineNum(task.getLineNum());
		cmd.setCarNum((short) task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable first = timetableList.get(1);//第二条数据为车站数据
		
		TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
		String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
		
		cmd.setDstStationNum(endPlatformId);
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setDirectionPlan(task.getRunDirection()); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		cmd.setNextStationId(first.getPlatformId());
		cmd.setStationStopTime(0xFFFF); //计划站停时间（单位：秒）
		cmd.setSkipStationId(0xFFFF);
		cmd.setSkipNextStation((short) 0xAA);
		cmd.setSectionRunLevel(0xFFFF);
		
		//判断下一站是否人工设置跳停命令
		String skipStatusStr = null;
		try{
			skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, first.getPlatformId());
			if(skipStatusStr != null && skipStatusStr.equals("1")){//有跳停
				cmd.setSkipStationId(first.getPlatformId());
				cmd.setSkipNextStation((short) 0x55);
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
		
		
		cmd.setDetainCmd((short) 0);
		cmd.setReturnCmd((short) 0);
		cmd.setGotoRailYard((short) 0);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		
		return cmd;
	}
	
	/**
	 * 当列车到站(不管是否停稳)后，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向VOBC发送下一区间运行命令：设置区间运行等级
	 * @param event
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public AppDataATOCommand appDataATOCommandEnter(TrainRunTask task, TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {
		AppDataATOCommand cmd = new AppDataATOCommand();

		int platformId = event.getStation();

		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		TrainRunTimetable currStation = null;
		TrainRunTimetable nextStation = null;
		int timeSectionRun = 0;	//下一站区间运行时间
		int timeStationStop = 0; // 当前车站站停时间（单位：秒）
		for (int i = 1; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
				
			}
		}
		
		timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
		timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）

		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setNextZcId(0);
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCarLineNum(task.getLineNum());
		cmd.setCarNum((short) task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
		String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
		
		cmd.setDstStationNum(endPlatformId);
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setDirectionPlan(task.getRunDirection()); // ??? need rungraph supply!
		
		//若当前车站是终点站，则只发当前车站站停时间
		if(currStation.getPlatformId() == 8){
			cmd.setSkipStationId(0xFFFF);
			cmd.setSkipNextStation((short) 0xAA);
			cmd.setNextStationId(0xFFFF);
			cmd.setStationStopTime(timeStationStop); //计划站停时间（单位：秒）
			cmd.setSectionRunLevel(timeSectionRun);
			//cmd.setSectionRunLevel(2);
		}
		//若当前车站不是终点站，则发当前车站站停时间，下一站台ID，下一站区间运行时间
		else{
			cmd.setNextStationId(currStation.getNextPlatformId());
			cmd.setStationStopTime(timeStationStop); //计划站停时间（单位：秒）
			// 判断下一站是否跳停？
			if (nextStation.isSkip()) {
				cmd.setSkipNextStation((short) 0x55);
				cmd.setSkipStationId(nextStation.getPlatformId());
			}
			else {
				cmd.setSkipStationId(0xFFFF);
				cmd.setSkipNextStation((short) 0xAA);
			}		
					
			// 区间运行等级/区间运行时间
			cmd.setSectionRunLevel(timeSectionRun);
		}
		
		cmd.setDetainCmd((short) 0);
		cmd.setReturnCmd((short) 0);
		cmd.setGotoRailYard((short) 0);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		if(mapDwellTime.containsKey(currStation.getPlatformId())){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(currStation.getPlatformId());
			if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
				timeStationStop = dwellTimeCommand.getTime();//设置停站时间
				cmd.setStationStopTime(timeStationStop);
			}
		}		
				
		//判断下一站是否人工设置跳停命令
		String skipStatusStr = null;
		try{
			skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, nextStation.getPlatformId());
			if(skipStatusStr != null && skipStatusStr.equals("1")){//有跳停
				cmd.setSkipStationId(nextStation.getPlatformId());
				cmd.setSkipNextStation((short) 0x55);
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
			
		//判断当前车站是否人工设置跳停命令
		String skipStatus = null;
		try{
			skipStatus = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, currStation.getPlatformId());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				cmd.setStationStopTime(0x0001); //计划站停时间（单位：秒）
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
				
				
		return cmd;
	}

	
	/**
	 * 当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param event
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public AppDataStationTiming appDataStationTiming(TrainRunTask task, TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {

		int platformId = event.getStation();
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		TrainRunTimetable currStation = null;
		for (int i = 0; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
			}
		}
		int timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
		
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		appDataStationTiming.setStation_id(currStation.getPlatformId());
		appDataStationTiming.setTime(timeStationStop); //计划站停时间（单位：秒）
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		if(mapDwellTime.containsKey(currStation.getPlatformId())){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(currStation.getPlatformId());
			if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
				timeStationStop = dwellTimeCommand.getTime();//设置停站时间
				appDataStationTiming.setTime(timeStationStop);
			}
		}		
		
		//判断是否人工设置跳停命令
		String skipStatus = null;
		try{
			skipStatus = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, currStation.getPlatformId());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				appDataStationTiming.setTime(0x0001);; //设置站停时间（单位：秒）
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
		
				
		return appDataStationTiming;
	}
	
	
	/**
	 * 当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param event
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public AppDataStationTiming appDataStationTimingUnplan(TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {

		int platformId = event.getStation();

		int timeStationStop = 30; // 当前车站站停时间（单位：秒）默认
		
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		appDataStationTiming.setStation_id(platformId);
		appDataStationTiming.setTime(timeStationStop); //计划站停时间（单位：秒）
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		if(mapDwellTime.containsKey(platformId)){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(platformId);
			if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
				timeStationStop = dwellTimeCommand.getTime();//设置停站时间
				appDataStationTiming.setTime(timeStationStop);
			}
		}		
		
		//判断是否人工设置跳停命令
		String skipStatus = null;
		try{
			skipStatus = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				appDataStationTiming.setTime(0x0001);; //设置站停时间（单位：秒）
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
		
				
		return appDataStationTiming;
	}
	
	
	/**
	 * (非计划车)当列车到站(不管是否停稳)后，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向VOBC发送当前车站(默认)站停时间
	 * @param event
	 * @return
	 */
	public AppDataATOCommand appDataATOCommandEnterUnplan(TrainEventPosition event) {
		AppDataATOCommand cmd = new AppDataATOCommand();

		int platformId = event.getStation();

		int timeSectionRun = 0;	//下一站区间运行时间
		int timeStationStop = 30; // 当前车站站停时间（单位：秒）
		
		cmd.setServiceNum((short) 0xFF);
		cmd.setLineNum((short) 64); // ??? need rungraph supply!
		cmd.setNextZcId(0);
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCarLineNum((short) 64);
		cmd.setCarNum(event.getCarNum());
		cmd.setSrcLineNum((short) 64); // ??? need rungraph supply!
		cmd.setTrainNum((short) 0000);
		cmd.setDstLineNum((short) 64); // ??? need rungraph supply!
		
		cmd.setDstStationNum(String.valueOf(0xFFFF));
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setDirectionPlan((short) 0xFF); // ??? need rungraph supply!
		
		cmd.setSkipStationId(0xFFFF);
		cmd.setSkipNextStation((short) 0xAA);
		//cmd.setNextStationId(0xFFFF);
		cmd.setStationStopTime(30); //计划站停时间（单位：秒）默认30s
		cmd.setSectionRunLevel(0);
		
		cmd.setDetainCmd((short) 0);
		cmd.setReturnCmd((short) 0);
		cmd.setGotoRailYard((short) 0);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		if(mapDwellTime.containsKey(event.getStation())){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(event.getStation());
			if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
				timeStationStop = dwellTimeCommand.getTime();//设置停站时间
				cmd.setStationStopTime(timeStationStop);
			}
		}		
		
		//判断下一站是否人工设置跳停命令
		Integer nextPlatformId = event.getStation() + 1;
		if(event.getStation().equals(8)){
			nextPlatformId = 1;
		}
		
		cmd.setNextStationId(nextPlatformId);
		
		String skipStatusStr = null;
		try{
			skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, nextPlatformId);
			if(skipStatusStr != null && skipStatusStr.equals("1")){//有跳停
				cmd.setSkipStationId(nextPlatformId);
				cmd.setSkipNextStation((short) 0x55);
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
		
		//判断当前车站是否人工设置跳停命令
		String skipStatus = null;
		try{
			skipStatus = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, event.getStation());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				cmd.setStationStopTime(0x0001); //计划站停时间（单位：秒）
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or runtask parse error!");
			e.printStackTrace();
		}
					
				
		return cmd;
	}
	
	
	
	/**
	 * 当列车到达转换轨时，收到运行图发来的运行信息，根据车次时刻表向VOBC发送任务命令（表号、车组号、车次号信息）
	 * @param task
	 * @return
	 */
	public AppDataATOCommand appDataATOCommandTask(TrainRunInfo trainRunInfo) throws Exception {
		AppDataATOCommand cmd = new AppDataATOCommand();

		cmd.setServiceNum((short) trainRunInfo.getTablenum());
		cmd.setLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setNextZcId(0);//0xffffffff
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCarLineNum((short) trainRunInfo.getLineNum());
		cmd.setCarNum((short) trainRunInfo.getTraingroupnum());
		cmd.setSrcLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) trainRunInfo.getTrainnum());
		cmd.setDstLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		
		cmd.setDstStationNum("0");	//填啥？
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setDirectionPlan(trainRunInfo.getRunDirection()); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		cmd.setNextStationId(0xFFFF);
		cmd.setStationStopTime(0xFFFF); //计划站停时间（单位：秒）
		cmd.setSkipStationId(0xFFFF);
		cmd.setSkipNextStation((short) 0xAA);
		cmd.setSectionRunLevel(0xFFFF);
		
		cmd.setDetainCmd((short) 0);
		cmd.setReturnCmd((short) 0);
		cmd.setGotoRailYard((short) 0);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		
		return cmd;
	}
	
	
	
	
	/**
	 * 获取运行图服务的运行任务
	 * @param carNum 车组号
	 * @param tablenum 表号
	 * @param trainnum 车次号
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 *//*
	@HystrixCommand(fallbackMethod = "connectTrainrungraphError")
	public void getRuntask(Integer carNum, short tablenum, short trainnum) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objMapper = new ObjectMapper();
		String resultMsg = null;
		resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, carNum, tablenum, trainnum);
		try{
			if(resultMsg != null){
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
		
	}
	
	*//**
	 * 获取列车控制服务的站台跳停状态
	 * @param platformId 站台ID
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 *//*
	@HystrixCommand(fallbackMethod = "connectTraincontrolError")
	public Integer getSkipStationStatus(Integer platformId) throws JsonParseException, JsonMappingException, IOException{
		String skipStatusStr = null;
		skipStatusStr = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
		
		Integer skipStatus = Integer.getInteger(skipStatusStr);
		return skipStatus;
	}
	
	*//**
	 * 获取运行图服务的所有站台停站时间
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 *//*
	@HystrixCommand(fallbackMethod = "connectTrainrungraphError")
	public void getDwellTimeList() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objMapper = new ObjectMapper();
		String resultMsg = null;
		List<AppDataDwellTimeCommand> dataList = new ArrayList<AppDataDwellTimeCommand>();
		
			resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntaskAllCommand", String.class);
			try{
				if(resultMsg != null){
					dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
					for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
						mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
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
	
	*//**
	 * 保存某个站台停站时间至运行图服务中
	 * @param dwellTimeCommandJson 设置停站时间命令字符串
	 * @return
	 *//*
	@HystrixCommand(fallbackMethod = "connectTrainrungraphError")
	public String saveRuntaskCommand(String dwellTimeCommandJson){
		String resultMsg = null;
		resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/saveRuntaskCommand?json={json}", String.class, dwellTimeCommandJson);
		return resultMsg;
	}
	
	public void connectTrainrungraphError() {
		LOG.error("---[serv31-trainrungraph] can't connection!---");
	}
	
	public void connectTraincontrolError() {
		LOG.error("---[serv35-traincontrol] can't connection!---");
	}*/
}
