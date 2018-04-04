package com.byd5.ats.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.service.hystrixService.TraincontrolHystrixService;
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RuntaskUtils {
	private static final Logger LOG = LoggerFactory.getLogger(RuntaskUtils.class);

	@Autowired
	private TraincontrolHystrixService traincontrolHystrixService;
	@Autowired
	private TrainrungraphHystrixService trainrungraphHystrixService;
	
	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainRunTask> mapRunTask = new HashMap<Integer, TrainRunTask>();
	
	/**
	 * 设置停站时间map：以站台ID为key，AppDataDwellTimeCommand类为value
	 */
	public Map<Integer, AppDataDwellTimeCommand> mapDwellTime = new HashMap<Integer, AppDataDwellTimeCommand>();
	
	/**
	 * 车站扣车状态列表，3为无扣车，小于3为有扣车
	 */
	public List<Byte> listDtStatus = new ArrayList<Byte>();
	/**
	 * 列车位置信息map：以车组号为key，ATO命令AppDataAVAtoCommand类为value
	 */
	public Map<Integer, AppDataAVAtoCommand> mapAtoCmd = new HashMap<Integer, AppDataAVAtoCommand>();
	
	/**初始化扣车状态数组大小，默认所有车站扣车状态为未扣车(3)*/
	public RuntaskUtils()
	{
		for(int i=0;i<8;i++)
		{
			this.listDtStatus.add((byte) 3);
		}
		
	}
	
	/**
	 * 设置目的地号: 类型转换String->char[]
	 * 
	 * @param dstStationNum 运行图目的地号
	 * @return 转换后的目的地号
	 */
	public char[] convertDstCode2Char(String dstStationNum) {
		int len = 4;
		char[] code = {' ', ' ', ' ', ' '};
		if(dstStationNum != null){
			char[] dst = dstStationNum.toCharArray();
			if (dst.length < len) {
				len = dst.length;
			}
			for (int i = 0; i < len; i ++) {
				code[3-i] = dst[len-1-i];
			}
		}
		return code;
	}
	
	
	/**
	 * 当前车站人工设置跳停命令,停站时间为0
	 * @param platformId 站台ID
	 * @return 停站时间	若有跳停则停站时间为0，否则返回null
	 */
	public Integer getStopTimeIfSkipCurr(int platformId){
		Integer result = null;
		String skipStatus = traincontrolHystrixService.getSkipStationStatus(platformId);
		if(skipStatus != null && skipStatus.equals("1")){//1:有跳停,0:无跳停
			//cmd.setStationStopTime(0x0001); //计划站停时间（单位：秒）
			result = 0xFFFF;
		}
		return result;
	}
	

	/** 下一站有折返，设置折返命令: 
	 * 		站前折返：0x55<br>有人站后折返：0xCC<br>无人自动折返：0xAA<br>不折返：0xFF
	 * @param nextStation 下一站信息
	 * @return 折返命令
	 */
	public short covertTurnbackCmd(TrainRunTimetable nextStation) {
		
		short turnbackCmd = 0xFF;
		if(nextStation !=null && nextStation.getReturnMode() == 1){
			turnbackCmd = 0xCC;
		}
		return turnbackCmd;
	}
	
	/**
	 * (若下一站有跳停)设置下一停车站台ID、停站时间
	 * @param cmd ATO命令信息
	 * @param currStation 当前站台信息
	 * @param timetableList 车次时刻表
	 * @return ATO 命令信息
	 */
	public AppDataAVAtoCommand setNextStopStation(AppDataAVAtoCommand cmd, TrainRunTimetable currStation, TrainRunTask task) {
		AppDataAVAtoCommand atoCmd = cmd;
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		if(atoCmd.getNextSkipCmd() == 0x55  
				&& currStation.getPlatformId()!= 0 
				&& currStation.getPlatformId() != 9){//有跳停,则获取跳停站台后的第一个停车站台
			int nextPlatformId = currStation.getNextPlatformId();
			for (int i = 1; i < timetableList.size(); i ++) {//获取下一停车站台ID
				TrainRunTimetable nextStation = timetableList.get(i);
				if (nextStation.getPlatformId() == nextPlatformId){
					String skipStatusStr1 = getSkipStationStatus(nextPlatformId);
					System.out.println("platformId:"+nextPlatformId+" skipStatus:"+skipStatusStr1);
					if(skipStatusStr1 == null){
						return null;
					}
					if(!nextStation.isSkip() && !(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						atoCmd.setPlatformStopTime((int) nextStation.getStopTime());
						break;
					}
					/*if(nextPlatformId == 6 && (nextStation.isSkip() || skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						atoCmd.setPlatformStopTime((int) nextStation.getStopTime());
						break;
					}*/
					if (nextPlatformId == DstCodeEnum.getPlatformIdByDstCode(task.getDstStationNum())
							&& (nextStation.isSkip() || skipStatusStr1 != null && skipStatusStr1.equals("1"))) {
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						atoCmd.setPlatformStopTime((int) nextStation.getStopTime());
						break;
					}
					nextPlatformId = nextStation.getNextPlatformId();
					currStation = timetableList.get(i);
				}
			}
		}
		return atoCmd;
	}
	
	/**
	 * 下一站跳停状态处理： 设置跳停命令、跳停站台ID，应满足以下条件：
	 * 		1、有人工设置跳停命令
	 * 		2、或者运行计划有跳停
	 * 无跳停：设置无跳停命令、下一停车站台ID
	 * @param cmd ATO命令信息
	 * @param nextStation 下一站台信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand nextStaionSkipStatusProccess(AppDataAVAtoCommand cmd, TrainRunTimetable nextStation){
		//下一站有人工设置跳停命令或者运行计划有跳停
		AppDataAVAtoCommand atoCmd = cmd;
		/**从运行控制模块获取站台跳停状态信息*/
		String skipStatusStr = getSkipStationStatus(nextStation.getPlatformId());
		LOG.info("[StaionSkipStatus] platformId:{} skipStatus:{}", nextStation.getPlatformId(), skipStatusStr);
		if(nextStation.getPlatformId() != 6 
				&& skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
				|| nextStation.isSkip()){//运行计划有跳停
			atoCmd.setNextSkipCmd((short) 0x55);
			atoCmd.setSkipPlatformId(nextStation.getPlatformId());
		}
		else {//下一站无跳停
			atoCmd.setNextSkipCmd((short) 0xAA);
			atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
		}
		return atoCmd;
	}
	
	/**
	 * 设置下一站停车站台ID(若有跳停)
	 * @param cmd
	 * @param platformId
	 * @param trainDir
	 * @param dstCode
	 * @return
	 */
	public AppDataAVAtoCommand setNextStopPlatformId(AppDataAVAtoCommand cmd, Integer platformId, short trainDir, String dstCode) {
		AppDataAVAtoCommand atoCmd = cmd;
		if(atoCmd.getNextSkipCmd() == 0x55  
				&& platformId!= 0 
				&& platformId != 9){//有跳停,则获取跳停站台后的第一个停车站台
			Integer nextPlatformId = traincontrolHystrixService.getNextPlatformId(trainDir, platformId);
			if(nextPlatformId == null){
				return null;
			}
			String skipStatusStr1 = getSkipStationStatus(nextPlatformId);
			System.out.println("platformId:"+nextPlatformId+" skipStatus:"+skipStatusStr1);
			if(!(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
				atoCmd.setNextStopPlatformId(nextPlatformId);
				Integer stoptime = getDefDwellTime(nextPlatformId);
				if(stoptime == null){
					return null;
				}
				cmd.setPlatformStopTime(stoptime);
			}
			/*else if(dstCodeEnum != null && nextPlatformId == dstCodeEnum.getPlatformId()
					&& skipStatusStr1 != null && skipStatusStr1.equals("1")) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
				atoCmd.setNextStopPlatformId(nextPlatformId);
				cmd.setPlatformStopTime(getDefDwellTime(nextPlatformId));
				//atoCmd.setSectionRunAdjustCmd((short) runtime);
			}*/
			else{
				atoCmd = setNextStopPlatformId(atoCmd, nextPlatformId, trainDir, dstCode);
			}
		}
		return atoCmd;
	}
	
	/**
	 * 计划当前车次时刻表的站停时间、区间运行时间
	 * 
	 * @param runtask 当前车次时刻表
	 * @return 最终的时刻表
	 */
	public TrainRunTask calculateTime(TrainRunTask runtask){
		List<TrainRunTimetable> timetableList = runtask.getTrainRunTimetable();
		if(timetableList != null && timetableList.size() > 0){
			for (int i = 1; i < timetableList.size(); i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
				TrainRunTimetable prevStation = timetableList.get(i-1);
				TrainRunTimetable thisStation = timetableList.get(i);
				thisStation.setStopTime((thisStation.getPlanLeaveTime() - thisStation.getPlanArriveTime())/1000);//当前站停站时间
				thisStation.setRunTime((thisStation.getPlanArriveTime() - prevStation.getPlanLeaveTime())/1000);
				runtask.getTrainRunTimetable().set(i, thisStation);
			}
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 1);
			int stoptime = 30;//默认停站时间
			TrainRunTask newtask = trainrungraphHystrixService.getNextRuntask(runtask.getTraingroupnum(), runtask.getTablenum(), 
					runtask.getTrainnum(), lastStation.getPlatformId());
			if(newtask != null){
				TrainRunTimetable firstStation = newtask.getTrainRunTimetable().get(0);
				int stoptimes = (int) ((firstStation.getPlanLeaveTime() - lastStation.getPlanArriveTime())/1000);
				if(0 < stoptimes && stoptimes < 100){
					stoptime = stoptimes;
				}
			}
			else{
				LOG.error("获取下一运行任务失败，不存在! 终点站默认停站时间："+stoptime);
			}
			lastStation.setStopTime(stoptime);//当前站停站时间
			runtask.getTrainRunTimetable().set(timetableList.size()-1, lastStation);
		}
		return runtask;
	}
	
	/**
	 * 如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
	 * @param platformId 站台ID
	 * @return 停站时间	有人工设置则返回停站时间，否则返回null
	 */
	public Integer getStopTimeCmdCurr(int platformId){
		Integer result = null;
		if(mapDwellTime.containsKey(platformId)){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(platformId);
			if(dwellTimeCommand != null && dwellTimeCommand.getSetWay() == 0){//0为人工设置
				result = dwellTimeCommand.getTime();
			}
		}
		return result;
	}
	
	/**
	 * 下一停车站台有扣车，设置有扣车命令
	 * @param cmd ATO命令信息
	 * @return ATO命令信息
	 */
	public short getDtStatusCmd(Integer platformId) {
		/**下一停车站台ID有扣车3，则设置有扣车命令0x55*/
		short detainCmd = 0xAA;
		if(platformId != 0 && platformId != 9){//车站扣车状态,不包括折返轨和转换轨
			LOG.info("[getDtStatusCmd] platformId:{} DtStatus:{}", platformId, listDtStatus.get(platformId-1));
			if(platformId <= 8 && listDtStatus.get(platformId-1) < 3){
				detainCmd =  0x55;
			}
		}
		return detainCmd;
	}
	
	public String getSkipStationStatus(Integer platformId){
		return traincontrolHystrixService.getSkipStationStatus(platformId);
	}
	
	public Integer getDefRunTime(Integer platformId){
		return traincontrolHystrixService.getDefRunTime(platformId);
	}
	
	public Integer getDefDwellTime(Integer platformId){
		return traincontrolHystrixService.getDefDwellTime(platformId);
	}
	
	/**获取所有车站停站时间*/
	public void getmapDwellTime(){
		if(mapDwellTime.size() == 0){
			List<AppDataDwellTimeCommand> dataList = trainrungraphHystrixService.getDwellTime();
			if(dataList != null){
				for(AppDataDwellTimeCommand dwellTimeCmd:dataList){
					mapDwellTime.put(dwellTimeCmd.getPlatformId(), dwellTimeCmd);
				}
			}else{
				LOG.error("[getmapDwellTime] serv31-trainrungraph fallback getDwellTime is null!");
			}
		}
	}
	
	
	
	/**
	 * 更新列车对应的ATO命令
	 * @param cmd
	 */
	public void updateAtoCmd(AppDataAVAtoCommand cmd) {
		try {
			ObjectMapper mapper = new ObjectMapper(); // 转换器
			LOG.info("[updateAtoCmd] " + mapper.writeValueAsString(cmd));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		if(cmd != null){
			if(mapAtoCmd.containsKey(cmd.getCargroupNum())){
				mapAtoCmd.replace(cmd.getCargroupNum(), cmd);
			}
			else{
				mapAtoCmd.put(cmd.getCargroupNum(), cmd);
			}
		}
	}
	
	/**
	 * 移除车组号对应的列车ATO命令
	 * @param groupnum
	 */
	public void removeAtoCmd(Integer groupnum) {
		mapAtoCmd.remove(groupnum);
	}
	
	/**
	 * 更新运行任务列表
	 * @param carNum 车组号
	 * @param runTask 运行任务信息
	 */
	public void updateMapRuntask(Integer carNum, TrainRunTask runTask){
		//计算停站时间和区间运行时间s
		runTask = calculateTime(runTask);
		
		if (!mapRunTask.containsKey(carNum)) {
			mapRunTask.put(carNum, runTask);
		}
		else {
			mapRunTask.replace(carNum, runTask);
		}
	}
	
	/**
	 * 获取车组号对应运行任务
	 * @param carNum 车组号
	 * @return 运行任务信息
	 */
	public TrainRunTask getRuntask(Integer carNum){
		if (mapRunTask.containsKey(carNum)) {
			return mapRunTask.get(carNum);
		}
		return null;		
	}
	
	/**
	 * (计划车)获取运行任务信息
	 * @param event 列车位置信息
	 * @return 运行任务信息
	 */
	public TrainRunTask getMapRuntaskPlan(TrainEventPosition event){
		return getNewRuntask(event);
	}
	
	/**
	 * 移除计划车离站后，移除位置信息，防止离站后，对该站设置跳停扣车时产生影响
	 * @param event 列车位置信息
	 */
	public void removeMapRuntask(TrainEventPosition event){
		if (event.getServiceNum() == 0) {//非计划车，则移除
			mapRunTask.remove(event.getCargroupNum());
		}
	}
	
	/**
	 * (车组号不为0)根据车组号、表号和车次号获取列车运行任务信息
	 * @param event 列车位置信息
	 * @return 运行任务信息
	 */
	public TrainRunTask getNewRuntask(TrainEventPosition event){
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		int carNum = event.getCargroupNum();
		
		//1、该车为计划车：
		//	1)任务列表为空
		//  2)(或者)任务列表不为空且车组号相同车次号不同
		//	3)(或者)任务列表不为空且无该车组号对应的任务列表
		//2、从运行图服务中获取新的任务列表
		Boolean isDiffTrainnum = mapRunTask.size() > 0 && mapRunTask.containsKey(carNum) && mapRunTask.get(carNum).getTrainnum() != event.getTrainNum();//  2)(或者)任务列表不为空且车组号相同车次号不同
		Boolean isExcludeCarNum = mapRunTask.size() > 0 && !mapRunTask.containsKey(carNum);//	3)(或者)任务列表不为空且无该车组号对应的任务列表
		if(event.getServiceNum() != 0 && (mapRunTask.size() == 0 || isDiffTrainnum || isExcludeCarNum))//&& !"ZH".equals(dsStationNum)
			{
			TrainRunTask newtask = trainrungraphHystrixService.getRuntask(event);
			if(newtask != null ){
				updateMapRuntask(carNum, newtask);//更新运行任务列表
				return newtask;
			}
			else{
				LOG.error("获取运行任务失败，不存在");
			}
		}
		else{
			return getRuntask(carNum);//返回已存在的运行任务信息
		}
		return null;
	}
	
	/**
	 * 获取当前车组号对应的任务信息，非计划车返回null
	 * @param event 列车位置信息
	 * @return 运行任务信息
	 */
	public TrainRunTask getMapRuntask(TrainEventPosition event){
		//获取停站时间列表
		getmapDwellTime();
		
		if(event.getServiceNum() != 0){//计划车
			//获取或 更新运行图任务信息
			return getMapRuntaskPlan(event);
		}
		else {//非计划车的处理
			removeMapRuntask(event);//移除历史记录
			LOG.info("[getMapRuntask] unplanTrain----");
		}
		return null;
	}
	
	public String saveRuntaskCommand(String dwellTimeCmd){
		return trainrungraphHystrixService.saveRuntaskCommand(dwellTimeCmd);
	}
	
	public List<TrainEventPosition> getAllTrainStatus(){
		return traincontrolHystrixService.getAllTrainStatus();
	}
	
	public Integer convertNextPlatformId(Integer nextPlatformId){
		if(nextPlatformId == 10){//下一站转换轨
			nextPlatformId = 0;
		}
		
		if(nextPlatformId == 9){//下一站折返轨
			nextPlatformId = 9;
		}
		return nextPlatformId;
	}
	
	//**初始化值AtoCommand*//*
		public AppDataAVAtoCommand initAtoCommand(){
			AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
			cmd.setType((short) 0x0203);
			cmd.setLength((short) 50);
//			cmd.setServiceNum(0xFF);
			cmd.setServiceNum(0);
			cmd.setLineNum(0x40);
			cmd.setNextZcId(0);;
			cmd.setNextCiId(0);
			cmd.setNextAtsId(0);
			cmd.setCargroupLineNum(0x40);
			cmd.setCargroupNum((short) 0);
			cmd.setSrcLineNum(0xFFFF);//?0x40
			cmd.setTrainNum((short) 0x0000);
			cmd.setDstLineNum(0xFFFF);
			char[] dstCode = {' ',' ',' ',' '};
			cmd.setDstCode(dstCode);//0xFFFF
			cmd.setPlanDir((short) 0xFF);
			cmd.setNextStopPlatformId(0xFFFF);
			cmd.setPlatformStopTime(0);
			cmd.setSkipPlatformId(0xFFFF);
			cmd.setNextSkipCmd((short) 0xAA);
			cmd.setSectionRunAdjustCmd(0);
			cmd.setDetainCmd((short) 0xAA);
			cmd.setTurnbackCmd((short) 0xFF);//站前折返：0x55；有人站后折返：0xCC；无人自动折返：0xAA；不折返：0xFF
			cmd.setBackDepotCmd((short) 0xFF);	//回段：0x55 不回段：0xAA 默认值：0xFF[注5]当列车最大安全前端不在转换轨内或列车不为回段方向时， ATS向VOBC发送的“回段指示”字段为默认值；
												//当列车最大安全前端在转换轨内且列车为回段方向时，对于计划列车，ATS根据列车运行计划，向VOBC发送“回段”或“不回段”提示，对于非计划列车，ATS向VOBC发送默认值；
												//VOBC与ATS通信正常且收到的回段提示字段非默认值时，根据ATS回段提示信息，判断在转换轨内是否显示回段提示；VOBC与ATS通信断开或收到的回段提示字段为默认值时，根据电子地图配置的区段属性，在转换轨内显示回段提示。
			cmd.setDoorctrlStrategy((short) 0xFF);
			cmd.setReserved(0);
			return cmd;
		}
		
		/**
		 * 获取当前站的计划信息
		 * @param task 当前车次时刻表
		 * @param platformId 站台ID
		 * @return 当前站的计划信息
		 */
		public TrainRunTimetable getCurrStationPlan(TrainRunTask task, Integer platformId) {
			TrainRunTimetable currStation = null;
			/**当前车次对应的时刻表信息*/
			List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
			/**获取当前车次时刻表的对应当前站台、下一站台的信息*/
			for (int i = 1; i < timetableList.size(); i++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
				TrainRunTimetable t = timetableList.get(i);
				if (t.getPlatformId() == platformId) {
					currStation  = t;
					break;
				}
			}
			return currStation;
		}
}
