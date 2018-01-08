package com.byd5.ats.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunInfo;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.service.hystrixService.TraincontrolHystrixService;
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
import com.byd5.ats.utils.DstCodeEnum;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 列车运行任务处理类
 * @author wu.xianglan
 *
 */
@Component
public class RunTaskService{
	private static final Logger LOG = LoggerFactory.getLogger(RunTaskService.class);

	@Autowired
	private TraincontrolHystrixService traincontrolHystrixService;
	
	@Autowired
	private TrainrungraphHystrixService rungraphHystrixService;
	
	@Autowired
	private RuntaskUtils runtaskUtils;
	
	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainRunTask> mapRunTask = new HashMap<Integer, TrainRunTask>();

	/**
	 * 列车位置信息map：以车组号为key，TrainTrace类为value
	 */
	public Map<Integer, TrainEventPosition> mapTrace = new HashMap<Integer, TrainEventPosition>();
	
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
	public RunTaskService()
	{
		for(int i=0;i<8;i++)
		{
			this.listDtStatus.add((byte) 3);
		}
		
	}
	
	/**
	 * 非计划车在区间运行时发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationSectionUnplan(TrainEventPosition event) {
		LOG.info("--getStationSectionUnplan--start");
		Integer platformId = event.getNextStationId();//下一站
		//1、跳停判断
		AppDataAVAtoCommand cmd = getStationSkipUnplan(event, platformId);
		LOG.info("--getStationSectionUnplan--end");
		return cmd;
	}
	
	/**
	 * 非计划车进站发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationEnterUnplan(TrainEventPosition event) {
		LOG.info("--getStationEnterUnplan--start");
		AppDataAVAtoCommand cmd = null;
		try{
			Integer platformId = event.getStation();//当前站
			//1、再次更新默认ATO的Map
			getStationUnplan(event, platformId);
			//2、扣车判断
			cmd = getStationDetainUnplan(event, platformId);
			if(cmd == null){
				return null;
			}
			//3、跳停判断
			if(cmd.getDetainCmd() != 0x55){
				cmd = getStationSkipUnplan(event, platformId);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--getStationEnterUnplan--end");
		return cmd;
	}
	
	/**
	 * 非计划车在站台上时发送ATO命令(停稳时，下发的命令只有设置/取消扣车)
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationArriveUnplan(TrainEventPosition event) {
		LOG.info("--getStationArriveUnplan--start");
		Integer platformId = event.getStation();//当前站
		//1、扣车判断
		AppDataAVAtoCommand cmd = getStationDetainUnplan(event, platformId);
		if(cmd == null){
			return null;
		}
		//2、跳停判断
		/*if(cmd.getDetainCmd() != 0x55){
			cmd = getStationSkipUnplan(event, platformId);
		}*/
		LOG.info("--getStationArriveUnplan--end");
		return cmd;
	}
	
	/**
	 * 非计划车离站
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationLeaveUnplan(TrainEventPosition event) {
		LOG.info("--getStationLeaveUnplan--start");
		Integer platformId = event.getNextStationId();//下一站
		//1、更新默认ATO为下一站的Map
		getStationUnplan(event, platformId);
		//2、跳停判断
		AppDataAVAtoCommand cmd = getStationSkipUnplan(event, platformId);
		LOG.info("--getStationLeaveUnplan--end");
		return cmd;
	}
	
	/**
	 * 非计划车，离站时，保存至公共map中，发当前站停站时间，下一站区间运行时间(默认无扣车跳停)
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationUnplan(TrainEventPosition event, Integer platformId) {
		// TODO Auto-generated method stub
		LOG.info("--getStationUnplan--start");
		/**ATO命令信息*/
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		/**初始化ATO命令数据为默认值*/
		cmd = initAtoCommand(cmd);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setCargroupLineNum(event.getCargroupLineNum());
		cmd.setCargroupNum(event.getCargroupNum());
		cmd.setTrainNum(event.getTrainNum());
		/**设置目的地号为终点站站台ID*/
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(event.getDstCode()));
		cmd.setPlanDir((short) event.getTrainDir()); // ??? need rungraph supply!
		/** 下一站区间运行时间不会改变，除非有调整*/
		Integer runtime = traincontrolHystrixService.getDefRunTime(platformId);
		if(runtime == null){//获取失败，不下发ATO命令
			return null;
		}
		cmd.setSectionRunAdjustCmd(runtime);
		/**设置默认停车站台ID*/
		cmd.setNextStopPlatformId(platformId);
		Integer stopTime = getStopTimeCmdCurr(platformId);
		if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			cmd.setPlatformStopTime(stopTime);
		}
		else{//车站默认停站时间
			Integer dwelltime = traincontrolHystrixService.getDefDwellTime(platformId);
			if(dwelltime == null){
				return null;
			}
			cmd.setPlatformStopTime(dwelltime);
		}
		
		//更新ATO命令
		updateAtoCmd(cmd);
		LOG.info("--getStationUnplan--end");		
		return cmd;
	}
	
	/**
	 * 获取车站扣车状态命令(非计划车)
	 * @param event
	 * @param platformId 
	 * @return
	 */
	public AppDataAVAtoCommand getStationDetainUnplan(TrainEventPosition event, Integer platformId) {
		LOG.info("--getStationDetainUnplan--start");		
		AppDataAVAtoCommand cmd = getExistAtoCmd(event, platformId);
		if(cmd == null){
			return null;
		}
		/**当前站台有扣车，设置有扣车命令*/
		cmd.setDetainCmd(getDtStatusCmd(platformId));
		LOG.info("--getStationDetainUnplan--end");		
		return cmd;
		
	}
	
	/**
	 * 获取当前站台跳停状态命令(非计划车)
	 * @param event
	 * @param platformId 
	 * @return
	 */
	public AppDataAVAtoCommand getStationSkipUnplan(TrainEventPosition event, Integer platformId) {
		LOG.info("--getStationSkipUnplan--start");	
		AppDataAVAtoCommand cmd = null;
		try{
			cmd = getExistAtoCmd(event, platformId);
			if(cmd == null){
				return null;
			}
			
			//判断当前车站是否有跳停
			String skipStatusStr = traincontrolHystrixService.getSkipStationStatus(platformId);
			LOG.info("[getStationSkipUnplan] platformId:{} skipStatus:{}", platformId, skipStatusStr);
			if(skipStatusStr != null && skipStatusStr.equals("error")){
				return null;
			}
			if(skipStatusStr != null && skipStatusStr.equals("1")){//人工设置跳停
				cmd.setNextSkipCmd((short) 0x55);
				cmd.setSkipPlatformId(platformId);
				cmd = runtaskUtils.setNextStopPlatformId(cmd, platformId, event.getTrainDir(), event.getDstCode());//设置下一停车站台ID
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		LOG.info("--getStationSkipUnplan--end");		
		return cmd;
	}

	private AppDataAVAtoCommand getExistAtoCmd(TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = mapAtoCmd.get(event.getCargroupNum());
		if(cmd == null || cmd.getCargroupNum() != event.getCargroupNum()){
			getStationUnplan(event, platformId);
			cmd = mapAtoCmd.get(event.getCargroupNum());
		}
		AppDataAVAtoCommand result = null;
		if(cmd != null){
			result = new AppDataAVAtoCommand();
			BeanUtils.copyProperties(cmd, result);//拷贝MAP中存储的ATO命令
		}
		ObjectMapper mapper = new ObjectMapper(); // 转换器
		try {
			LOG.info("[getExistAtoCmd] " + mapper.writeValueAsString(cmd));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 更新列车对应的ATO命令
	 * @param cmd
	 */
	private void updateAtoCmd(AppDataAVAtoCommand cmd) {
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
	private void removeAtoCmd(Integer groupnum) {
		mapAtoCmd.remove(groupnum);
	}
	
	
	
	
	
	
	/**
	 * 当列车离开折返时，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param event 列车位置信息
	 * @param task	运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getReturnLeave(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--离开折返轨getReturnLeave--start");
		/**ATO命令信息*/
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		/**初始化ATO命令数据为默认值*/
		cmd = initAtoCommand(cmd);//初始化ATO命令数据
		cmd.setBackDepotCmd((short) 0xAA);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!

		/**设置目的地号为终点站站台ID*/
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		/**当前车次对应的时刻表信息*/
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable currStation = null;// 当前车次的当前站台信息
		/**获取当前车次时刻表的对应当前站台、下一站台的信息*/
		for (int i = 0; i < timetableList.size()-1; i++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == DstCodeEnum.getPlatformIdByPhysicalPt(event.getTrainHeaderAtphysical())) {
				currStation = timetableList.get(i+1);
				break;
			}
		}
		/**有当前站台的信息*/
		if(currStation != null){
			Integer platformId = currStation.getNextPlatformId();
			cmd.setNextStopPlatformId(platformId);
			cmd.setPlatformStopTime((int) currStation.getStopTime());// 站停时间（单位：秒）
			cmd.setSectionRunAdjustCmd((int) currStation.getRunTime());// 区间运行等级/区间运行时间（单位：秒）
			Integer stopTime = getStopTimeCmdCurr(platformId);
			if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
				cmd.setPlatformStopTime(stopTime);
			}
		}else{
			LOG.error("[getReturnLeave]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			LOG.info("--[getReturnLeave]--end");	
			return null;
		}
		
		/**下一站跳停状态处理： 设置跳停命令、下一跳停站台ID，应满足以下条件： 1、有人工设置跳停命令 2、或者运行计划有跳停*/
		cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, currStation);
		
		/**(下一站有跳停)设置下一停车站台ID、区间运行时间*/
		cmd = runtaskUtils.setNextStopStation(cmd, currStation, timetableList);
		
		/**有扣车，设置扣车命令*/
		//cmd.setDetainCmd(getDtStatusCmd(first.getNextPlatformId()));
		
		/** 下一站有折返，设置折返命令*/
		//cmd.setTurnbackCmd(runtaskUtils.covertTurnbackCmd(first));
		
		LOG.info("--离开折返轨[getReturnLeave]--end");
		return cmd;
	}
	/*public AppDataAVAtoCommand aodCmdReturn(TrainEventPosition event, TrainRunTask task) {
		LOG.info("--达折返aodCmdReturn--start");
		*//**ATO命令信息*//*
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		*//**初始化ATO命令数据为默认值*//*
		cmd = initAtoCommand(cmd);//初始化ATO命令数据
		cmd.setBackDepotCmd((short) 0xAA);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!

		*//** 当前车次对应的时刻表信息 *//*
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		*//** 当前车次的起始站信息 *//*
		TrainRunTimetable first = timetableList.get(1);// 第二条数据为车站数据
		*//** 当前车次的终点站信息 *//*
		TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);// 最后一条数据为车站数据

		*//**设置目的地号为终点站站台ID*//*
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		*//**下一站跳停状态处理： 设置跳停命令、下一跳停站台ID，应满足以下条件： 1、有人工设置跳停命令 2、或者运行计划有跳停*//*
		cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, first);
		
		*//**(下一站有跳停)设置下一停车站台ID、区间运行时间*//*
		cmd = runtaskUtils.setNextStopStation(cmd, first, timetableList);
		
		*//**有扣车，设置扣车命令*//*
		cmd.setDetainCmd(getDtStatusCmd(first.getNextPlatformId()));
		
		*//** 下一站有折返，设置折返命令*//*
		cmd.setTurnbackCmd(runtaskUtils.covertTurnbackCmd(first));
		
		LOG.info("--到达折返轨aodCmdReturn--end");
		return cmd;
	}*/
	
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
	
	
	
	/**
	 *  当列车进站，根据列车位置、车次时刻表向VOBC发送当前区间运行命令：设置区间运行等级
	 * @param task 运行任务
	 * @param event 列车位置
	 * @return ATO命令信息
	 */
	/*public AppDataAVAtoCommand aodCmdStationEnter(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--aodCmdStationEnter--start");
		*//**ATO命令信息*//*
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		*//**初始化ATO命令数据为默认值*//*
		cmd = initAtoCommand(cmd);
		cmd.setBackDepotCmd((short) 0xAA);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		int platformId = event.getStation();

		*//**当前车次对应的时刻表信息*//*
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		*//**当前车次的当前站台信息*//*
		TrainRunTimetable currStation = null;
		
		*//**当前车次的下一站台信息*//*
		TrainRunTimetable nextStation = null;
		
		int timeStationStop = 0;	//停站时间
		int timeSectionRun = 0;	//下一站区间运行时间
		
		*//**获取当前车次时刻表的对应当前站台、下一站台的信息*//*
		for (int i = 0; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
				break;
			}
		}
		
		*//**有当前站台、下一站台的信息*//*
		if(currStation != null && nextStation != null){
			*//**当前车次的终点站信息*//*
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
			
			timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
			
			*//**设置目的地号为终点站站台ID*//*
			cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));
			cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
			
			cmd.setPlatformStopTime(timeStationStop);
			cmd.setSectionRunAdjustCmd(timeSectionRun);// 区间运行等级/区间运行时间
			
			//判断当前车站是否有跳停
			*//**下一站有人工设置跳停命令或者运行计划有跳停,设置跳停命令、跳停站台ID*//*
			cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, currStation);
			
			*//**若当前车站是终点站，则只发当前车站站停时间*//*
			if(currStation.getPlatformId() == lastStation.getPlatformId()){
				cmd.setNextSkipCmd((short) 0xAA);
				cmd.setSectionRunAdjustCmd(0);// 区间运行等级/区间运行时间
				//cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
			}
			*//**若当前车站不是终点站，则发当前车站站停时间，下一站台ID，下一站区间运行时间*//*
			else{
				*//**(下一站有跳停)设置下一停车站台ID、区间运行时间*//*
				cmd = setNextStopStation(cmd, currStation, timetableList);
			}
			
			*//**(下一站有跳停)设置下一停车站台ID、区间运行时间*//*
			cmd = runtaskUtils.setNextStopStation(cmd, currStation, timetableList);
			
			*//** 下一站有折返，设置折返命令*//*
			cmd.setTurnbackCmd(runtaskUtils.covertTurnbackCmd(currStation));
			
			*//**下一停车站台有扣车，设置有扣车命令*//*
			cmd.setDetainCmd(getDtStatusCmd(currStation.getPlatformId()));
			//cmd = setDtCmd(cmd);
			
			if(cmd.getNextSkipCmd() == 0x55 || cmd.getDetainCmd() == 0x55){//有跳停或扣车
				cmd.setPlatformStopTime(0xFFFF);
			}
			else{
				LOG.error("[aodCmdStationEnter]--this trainnum {} doesn't have skipCmd or detainCmd in this station {}, so discard", event.getTrainNum(),event.getStation());
				LOG.info("--aodCmdStationEnter--end");		
				return null;
			}
				
		}else{
			LOG.error("[aodCmdEnter]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			LOG.info("--aodCmdStationEnter--end");	
			return null;
		}
			
		LOG.info("--aodCmdStationEnter--end");		
		return cmd;
	}*/
	
	
	/**
	 *  当列车到站(不管是否停稳)，根据列车位置、车次时刻表向VOBC发送下一区间运行命令：设置区间运行等级
	 * @param task 运行任务
	 * @param event 列车位置
	 * @return ATO命令信息
	 */
	/*public AppDataAVAtoCommand aodCmdStationLeave(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--aodCmdStationLeave--start");
		*//**ATO命令信息*//*
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		*//**初始化ATO命令数据为默认值*//*
		cmd = initAtoCommand(cmd);
		cmd.setBackDepotCmd((short) 0xAA);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		int platformId = event.getStation();

		*//**当前车次对应的时刻表信息*//*
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		*//**当前车次的当前站台信息*//*
		TrainRunTimetable currStation = null;
		
		*//**当前车次的下一站台信息*//*
		TrainRunTimetable nextStation = null;
		
		int timeSectionRun = 0;	//下一站区间运行时间
		int timeStationStop = 0; // 当前车站站停时间（单位：秒）
		
		*//**获取当前车次时刻表的对应当前站台、下一站台的信息*//*
		for (int i = 0; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
				break;
			}
		}
		
		*//**有当前站台、下一站台的信息*//*
		if(currStation != null && nextStation != null){
			timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
			*//**当前车次的终点站信息*//*
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
			
			*//**设置目的地号为终点站站台ID*//*
			String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
			cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));
			//cmd.setDstCode(endPlatformId.toCharArray());
			cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
			
			*//**若当前车站是终点站，则只发当前车站站停时间*//*
			if(currStation.getPlatformId() == lastStation.getPlatformId()){
				cmd.setNextSkipCmd((short) 0xAA);
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
			}
			*//**若当前车站不是终点站，则发当前车站站停时间，下一站台ID，下一站区间运行时间*//*
			else{
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
				cmd.setSectionRunAdjustCmd((short) timeSectionRun);// 区间运行等级/区间运行时间
				
				*//**下一站有人工设置跳停命令或者运行计划有跳停,设置跳停命令、跳停站台ID*//*
				cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, nextStation);	
				
				*//**(下一站有跳停)设置下一停车站台ID、区间运行时间*//*
				cmd = runtaskUtils.setNextStopStation(cmd, currStation, timetableList);
			}
			
			*//**1、先判断当前车站是否人工设置跳停命令*//*
			Integer stopTime = runtaskUtils.getStopTimeIfSkipCurr(platformId);
			if(stopTime != null){//有跳停
				cmd.setPlatformStopTime(stopTime);
			}
			*//**2、否则如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间*//*
			else{//
				stopTime = getStopTimeCmdCurr(platformId);
				if(stopTime != null){
					cmd.setPlatformStopTime(stopTime);
				}
			}
			
			*//** 下一站有折返，设置折返命令*//*
			cmd.setTurnbackCmd(runtaskUtils.covertTurnbackCmd(nextStation));
			
			//(下一站有跳停)设置下一停车站台ID、区间运行时间
			cmd = setNextStopStation(cmd, currStation, timetableList);
		}
		else{
			LOG.error("[aodCmdStationLeave]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			return null;
		}
		
		*//**下一停车站台有扣车，设置有扣车命令*//*
		cmd.setDetainCmd(getDtStatusCmd(nextStation.getPlatformId()));
		//cmd = setDtCmd(cmd);
		
		LOG.info("--aodCmdStationLeave--end");		
		return cmd;
	}*/
	
	/**
	 * * 当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param task 运行任务信息
	 * @param event 列车位置信息
	 * @return ATO命令信息
	 */
	public AppDataStationTiming appDataStationTiming(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--appDataStationTiming--start");
		int platformId = event.getStation();
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		appDataStationTiming.setStation_id(platformId);
		/**当前站台有扣车，不发发车倒计时*/
		/*short detainCmd = getDtStatusCmd(event.getStation());
		if(detainCmd == 0x55){//有扣车
			LOG.info("--aodCmdStationEnter--end");		
			return null;
		}*/
		
		AppDataAVAtoCommand cmd = mapAtoCmd.get(event.getCargroupNum());
		if(cmd != null && cmd.getNextStopPlatformId() == platformId){
			if(cmd.getSkipPlatformId() != platformId){//当前站无跳停，发倒计时
				appDataStationTiming.setTime(cmd.getPlatformStopTime());
			}
			else{//当前站有跳停，不发倒计时
				return null;
			}
			
		}
		else{
			List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
			TrainRunTimetable currStation = null;
			for (int i = 1; i < timetableList.size(); i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
				TrainRunTimetable t = timetableList.get(i);
				if (t.getPlatformId() == platformId) {
					currStation = t;
					break;
				}
			}
			if(currStation == null){
				LOG.error("[appDataStationTiming]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
				return null;
			}
			//int timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			appDataStationTiming.setTime(currStation.getStopTime()); //计划站停时间（单位：秒）
			
			//1、先判断当前车站是否人工设置跳停命令
			Integer stopTime = runtaskUtils.getStopTimeIfSkipCurr(platformId);
			if(stopTime != null && stopTime == 0xFFFF){//有跳停
//				appDataStationTiming.setTime(0x0000);//设置站停时间（单位：秒）
				return null;
			}
			else{//2、否则如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
				stopTime = getStopTimeCmdCurr(event.getStation());
				if(stopTime != null){
					appDataStationTiming.setTime(stopTime);
				}
			}
		}
		
		LOG.info("--appDataStationTiming--end");		
		return appDataStationTiming;
	}
	
	
	/**
	 * (非计划车)当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param event 列车位置信息
	 * @return ATO命令信息
	 */
	public AppDataStationTiming appDataStationTimingUnplan(TrainEventPosition event) {
		LOG.info("--appDataStationTimingUnplan--start");
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		
		/**当前站台有扣车，不发发车倒计时*/
		/*short detainCmd = getDtStatusCmd(event.getStation());
		if(detainCmd == 0x55){//有扣车
			LOG.info("--appDataStationTimingUnplan--end");		
			return null;
		}*/
		
		appDataStationTiming.setStation_id(event.getStation());
		
		AppDataAVAtoCommand cmd = mapAtoCmd.get(event.getCargroupNum());
		if(cmd != null && cmd.getNextStopPlatformId() == event.getStation()){
			appDataStationTiming.setTime(cmd.getPlatformStopTime());
		}
		else{
			Integer stopTime = getStopTimeCmdCurr(event.getStation());//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			if(stopTime != null){
				appDataStationTiming.setTime(stopTime);
			}
			else{
				stopTime = traincontrolHystrixService.getDefDwellTime(event.getStation());
				if(stopTime == null){
					return null;
				}
				appDataStationTiming.setTime(stopTime); //计划站停时间（单位：秒）
			}
		}
		LOG.info("--appDataStationTimingUnplan--end");		
		return appDataStationTiming;
	}
	
	
	/**
	 * 当列车出段到达转换轨时，根据车次时刻表向VOBC发送任务命令（表号、车组号、车次号信息）
	 * @param event 列车位置信息
	 * @param task 运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getTransformLeave(TrainRunTask task, TrainEventPosition event){
		LOG.info("--getTransformLeave--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		cmd = initAtoCommand(cmd);//初始化数据
		cmd.setBackDepotCmd((short) 0xAA);

		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum((short) task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum((short) task.getLineNum());
		cmd.setCargroupNum((short) task.getTraingroupnum());
		cmd.setSrcLineNum((short) task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum((short) task.getLineNum()); // ??? need rungraph supply!
		
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		/**当前车次对应的时刻表信息*/
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable currStation = null;// 当前车次的当前站台信息
		/**获取当前车次时刻表的对应当前站台、下一站台的信息*/
		for (int i = 0; i < timetableList.size()-1; i++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == DstCodeEnum.getPlatformIdByPhysicalPt(event.getTrainHeaderAtphysical())) {
				currStation = timetableList.get(i+1);
				break;
			}
		}
		/**有当前站台的信息*/
		if(currStation != null){
			Integer platformId = event.getNextStationId();
			cmd.setNextStopPlatformId(platformId);
			cmd.setPlatformStopTime((int) currStation.getStopTime());// 站停时间（单位：秒）
			cmd.setSectionRunAdjustCmd((int) currStation.getRunTime());// 区间运行等级/区间运行时间（单位：秒）
			Integer stopTime = getStopTimeCmdCurr(platformId);
			if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
				cmd.setPlatformStopTime(stopTime);
			}
		}else{
			LOG.error("[getTransformLeave]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			LOG.info("--getTransformLeave--end");	
			return null;
		}
		
		cmd.setNextSkipCmd((short) 0xAA);
		
		//有扣车，设置扣车命令
		/**下一停车站台有扣车，设置有扣车命令*/
		//cmd.setDetainCmd(getDtStatusCmd(nextStation.getPlatformId()));
		//cmd = setDtCmd(cmd);

		LOG.info("--getTransformLeave--end");
		return cmd;
	}
	/*public AppDataAVAtoCommand aodCmdTransform(TrainEventPosition event, TrainRunInfo trainRunInfo){
		LOG.info("--aodCmdTransform--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		cmd = initAtoCommand(cmd);//初始化数据
		cmd.setBackDepotCmd((short) 0xAA);

		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum((short) trainRunInfo.getTablenum());
		cmd.setLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum((short) trainRunInfo.getLineNum());
		cmd.setCargroupNum((short) trainRunInfo.getTraingroupnum());
		cmd.setSrcLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) trainRunInfo.getTrainnum());
		cmd.setDstLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		
		//cmd.setDstCode(0);	//填啥？
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(trainRunInfo.getDstStationNum()));
		cmd.setPlanDir((short) ((trainRunInfo.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		cmd.setNextSkipCmd((short) 0xAA);
		cmd.setSectionRunAdjustCmd((short) 0);//?
		
		//有扣车，设置扣车命令
		*//**下一停车站台有扣车，设置有扣车命令*//*
		//cmd.setDetainCmd(getDtStatusCmd(nextStation.getPlatformId()));
		//cmd = setDtCmd(cmd);

		LOG.info("--aodCmdTransform--end");
		return cmd;
	}*/

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
				//result.setStationStopTime(dwellTimeCommand.getTime());//设置停站时间
				result = dwellTimeCommand.getTime();
			}
		}
		return result;
	}

	
	/**
	 * 更新运行任务列表
	 * @param carNum 车组号
	 * @param runTask 运行任务信息
	 */
	public void updateMapRuntask(Integer carNum, TrainRunTask runTask){
		//计算停站时间和区间运行时间s
		runTask = runtaskUtils.calculateTime(runTask);
		
		if (!mapRunTask.containsKey(carNum)) {
			mapRunTask.put(carNum, runTask);
		}
		else {
			mapRunTask.replace(carNum, runTask);
		}
	}
	
	/**
	 * 更新列车位置信息列表
	 * @param event 列车位置信息
	 */
	public void updateMapTrace(TrainEventPosition event){
		Integer carNum = (int) event.getCargroupNum();
		if (!mapTrace.containsKey(carNum)) {
			mapTrace.put(carNum, event);
		}
		else {
			mapTrace.replace(carNum, event);
		}
	}
	
	/**
	 * 检查该车是否有记录
	 * @param carNum 车组号
	 * @return 列车位置信息
	 */
	public TrainEventPosition getMapTrace(Integer carNum){
		if (mapTrace.containsKey(carNum)) {
			return mapTrace.get(carNum);
		}
		return null;		
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
		//clearMapRuntask((int)event.getCarNum(), event.getServiceNum());//移除历史信息
		/*if (event.getServiceNum() == 0) {//非计划车，则移除
			mapRunTask.remove(event.getCarNum());
			return null;
		}*/
		return getNewRuntask(event);
	}
	
	/**
	 * 非计划车时，移除历史计划车运行任务信息
	 * @param event 列车位置信息
	 */
	public void removeMapTrace(TrainEventPosition event){
		mapTrace.remove(event.getCargroupNum());
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
	
	/**获取所有车站停站时间*/
	public void getmapDwellTime(){
		if(mapDwellTime.size() == 0){
			ObjectMapper objMapper = new ObjectMapper();
			//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
			objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			try{
				String resultMsg = rungraphHystrixService.getDwellTime();
				if(resultMsg != null && !resultMsg.equals("error")){
					List<AppDataDwellTimeCommand> dataList = objMapper.readValue(resultMsg, new TypeReference<List<AppDataDwellTimeCommand>>() {}); // json转换成map
					for(AppDataDwellTimeCommand AppDataDwellTimeCommand:dataList){
						mapDwellTime.put(AppDataDwellTimeCommand.getPlatformId(), AppDataDwellTimeCommand);
					}
				}else if(resultMsg == null){
					LOG.error("[getmapDwellTime] serv31-trainrungraph fallback getRuntaskAllCommand is null!");
				}
			}catch (Exception e) {
				// TODO: handle exception
				LOG.error("[getmapDwellTime] getRuntaskAllCommand parse error!");
				e.printStackTrace();
			}
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
			String resultMsg = rungraphHystrixService.getRuntask(event);
			if(resultMsg != null && !resultMsg.equals("error")){
				try{
					TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
					updateMapRuntask(carNum, newtask);//更新运行任务列表
					return newtask;
				}catch (Exception e) {
					LOG.error("[trace.station.enter] runtask parse error!");
					e.printStackTrace();
				}
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
	
	//**初始化值AtoCommand*//*
	public AppDataAVAtoCommand initAtoCommand(AppDataAVAtoCommand cmd){
		cmd.setType((short) 0x0203);
		cmd.setLength((short) 50);
//		cmd.setServiceNum(0xFF);
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
		cmd.setTurnbackCmd((short) 0xFF);//站前折返：0x55
										//有人站后折返：0xCC
										//无人自动折返：0xAA
										//不折返：0xFF
		cmd.setBackDepotCmd((short) 0xFF);	//回段：0x55 不回段：0xAA 默认值：0xFF[注5]当列车最大安全前端不在转换轨内或列车不为回段方向时， ATS向VOBC发送的“回段指示”字段为默认值；
											//当列车最大安全前端在转换轨内且列车为回段方向时，对于计划列车，ATS根据列车运行计划，向VOBC发送“回段”或“不回段”提示，对于非计划列车，ATS向VOBC发送默认值；
											//VOBC与ATS通信正常且收到的回段提示字段非默认值时，根据ATS回段提示信息，判断在转换轨内是否显示回段提示；VOBC与ATS通信断开或收到的回段提示字段为默认值时，根据电子地图配置的区段属性，在转换轨内显示回段提示。
		cmd.setDoorctrlStrategy((short) 0xFF);
		cmd.setReserved(0);
		return cmd;
	}

	
	/** 获取计划车下一站台停站时间(重复以第一条数据为准)
	 * @param task
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStaionAodCmd(TrainRunTask task, TrainEventPosition event, Integer platformId) {
		LOG.info("--getStaionAodCmd--start");
		/**ATO命令信息*/
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		/**初始化ATO命令数据为默认值*/
		cmd = initAtoCommand(cmd);
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(task.getDstStationNum()));// 设置目的地号为终点站站台ID
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		cmd.setNextStopPlatformId(platformId);
		
//		int platformId = event.getStation();
//		task = runtaskUtils.calculateTime(task);//计算停站时间和区间运行时间s

		//当前车次的当前站台信息
		TrainRunTimetable currStation = getCurrStationPlan(task, platformId);
		
		/**有当前站台的信息*/
		if(currStation != null){
			cmd.setPlatformStopTime((int) currStation.getStopTime());// 站停时间（单位：秒）
			cmd.setSectionRunAdjustCmd((int) currStation.getRunTime());// 区间运行等级/区间运行时间（单位：秒）
			Integer stopTime = getStopTimeCmdCurr(platformId);
			if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
				cmd.setPlatformStopTime(stopTime);
			}
		}else{
			LOG.error("[getStaionAodCmd]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			LOG.info("--getStaionAodCmd--end");
			removeAtoCmd(event.getCargroupNum());//移除车组号对应的ATO命令缓存
			return null;
		}
			
		LOG.info("--getStaionAodCmd--end");
		updateAtoCmd(cmd);//更新车组号对应的ATO命令
		return cmd;
	}

	/**
	 * 获取当前站的计划信息
	 * @param task 当前车次时刻表
	 * @param platformId 站台ID
	 * @return 当前站的计划信息
	 */
	private TrainRunTimetable getCurrStationPlan(TrainRunTask task, Integer platformId) {
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
	
	/**
	 *  扣车命令
	 * @param task 运行任务
	 * @param event 列车位置
	 * @param platformId 
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getStationDetain(TrainRunTask task, TrainEventPosition event, Integer platformId) {
		LOG.info("--getStationDetain--start");
		AppDataAVAtoCommand cmd = null;
		try{
			//获取内存中保存的ATO命令
			cmd = getExistAtoCmdPlan(task, event, platformId);
			if(cmd == null){
				LOG.info("--getStationDetain--end");
				return null;
			}
			
			//判断是否有扣车
			/**站台有扣车，设置有扣车命令*/
			cmd.setDetainCmd(getDtStatusCmd(platformId));
			/*if(cmd.getDetainCmd() == 0x55){//有扣车,发送扣车命令
				//cmd.setPlatformStopTime(0xFFFF);
				return cmd;
			}
			else{//判断当前车站是否有跳停
				cmd = getStationSkip(task, event, platformId);
			}*/
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--getStationDetain--end");		
		return cmd;
	}
	
	/**
	 *  跳停命令
	 * @param task 运行任务
	 * @param event 列车位置
	 * @param platformId 
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getStationSkip(TrainRunTask task, TrainEventPosition event, Integer platformId) {
		LOG.info("--[getStationSkip]--start");
		AppDataAVAtoCommand cmd = null;
		try{
			//获取内存中保存的ATO命令
			cmd = getExistAtoCmdPlan(task, event, platformId);
			if(cmd == null){
				LOG.info("--[getStationSkip]--end");
				return null;
			}
			
			//判断当前车站是否有跳停
			TrainRunTimetable currStation = getCurrStationPlan(task, platformId);// 当前车次的当前站台信息
			
			if(currStation != null){
				/** 下一站有人工跳停命令或者计划有跳停,设置跳停命令、跳停站台ID
				 *  无跳停：设置无跳停命令、下一停车站台ID */
				/**从运行控制模块获取站台跳停状态信息*/
				String skipStatusStr = traincontrolHystrixService.getSkipStationStatus(platformId);
				LOG.info("[StaionSkipStatus] platformId:{} skipStatus:{}", platformId, skipStatusStr);
				if(skipStatusStr != null && skipStatusStr.equals("error")){
					return null;
				}
				if(currStation.getPlatformId() != 6 && skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
						|| currStation.isSkip()){//运行计划有跳停
					cmd.setNextSkipCmd((short) 0x55);
					cmd.setSkipPlatformId(platformId);
				}
				//cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, currStation);
				
				/**(下一站有跳停)设置下一停车站台ID、停站时间*/
				cmd = runtaskUtils.setNextStopStation(cmd, currStation, task.getTrainRunTimetable());
				if(cmd == null){
					return null;
				}
				if(cmd.getNextSkipCmd() == 0x55){
					Integer stopTime = getStopTimeCmdCurr(cmd.getNextStopPlatformId());
					if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
						cmd.setPlatformStopTime(stopTime);
					}
				}
			}
			else{
				LOG.error("[getStationSkip]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
				LOG.info("--[getStationSkip]--end");	
				return null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--[getStationSkip]--end");		
		return cmd;
	}
	
	/**
	 * 计划车在区间运行时发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationSection(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--[getStationSection]--start");
		AppDataAVAtoCommand cmd = null;
		try{
			Integer platformId = event.getNextStationId();//下一站
			//1、跳停判断
			cmd = getStationSkip(task, event, platformId);
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		LOG.info("--[getStationSection]--end");
		return cmd;
	}
	
	/**
	 * 计划车进站发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationEnter(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--[getStationEnter]--start");
		AppDataAVAtoCommand cmd = null;
		try{
			Integer platformId = event.getStation();//当前站
			//1、再次更新默认ATO的Map
			getStaionAodCmd(task, event, platformId);
			
			//2、扣车判断
			cmd = getStationDetain(task, event, platformId);
			if(cmd == null){
				return null;
			}
			//3、跳停判断
			if(cmd.getDetainCmd() != 0x55){
				cmd = getStationSkip(task, event, platformId);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		LOG.info("--[getStationEnter]--end");
		return cmd;
	}
	
	/**
	 * 计划车在站台上时发送ATO命令
	 * @param task
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationArrive(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--[getStationArrive]--start");
		AppDataAVAtoCommand cmd = null;
		try{
			Integer platformId = event.getStation();//当前站
			//1、扣车判断
			cmd = getStationDetain(task, event, platformId);
			if(cmd == null){
				return null;
			}
			//2、跳停判断
			/*if(cmd.getDetainCmd() != 0x55){
				cmd = getStationSkip(task, event, platformId);
			}*/
		}catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--[getStationArrive]--end");
		return cmd;
	}
	
	/**计划车离站发送ATO命令
	 * @param task
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationLeave(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--[getStationLeave]--start");
		AppDataAVAtoCommand cmd = null;
		try{
			TrainRunTimetable currStation = getCurrStationPlan(task, event.getStation());// 当前车次的当前站台信息
			if(currStation == null){
				LOG.error("[getStationLeave]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
				LOG.info("--[getStationLeave]--end");	
				return null;
			}
			Integer platformId = currStation.getNextPlatformId();//下一站
			
			//1、更新默认ATO为下一站的Map
			getStaionAodCmd(task, event, platformId);
			//2、跳停判断
			cmd = getStationSkip(task, event, platformId);
		}catch (Exception e) {
			// TODO: handle exception
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--[getStationLeave]--end");
		return cmd;
	}
	
	private AppDataAVAtoCommand getExistAtoCmdPlan(TrainRunTask task, TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = mapAtoCmd.get(event.getCargroupNum());
		if(cmd == null || cmd.getCargroupNum() != event.getCargroupNum()){
			getStaionAodCmd(task, event, platformId);
			cmd = mapAtoCmd.get(event.getCargroupNum());
		}
		AppDataAVAtoCommand result = null;
		if(cmd != null){
			result = new AppDataAVAtoCommand();
			BeanUtils.copyProperties(cmd, result);//拷贝MAP中存储的ATO命令
		}
		ObjectMapper mapper = new ObjectMapper(); // 转换器
		try {
			LOG.info("[getExistAtoCmdPlan] " + mapper.writeValueAsString(cmd));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
