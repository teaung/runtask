package com.byd5.ats.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.utils.DstCodeEnum;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TrainPlanService {
	private static final Logger LOG = LoggerFactory.getLogger(TrainPlanService.class);
	
	@Autowired
	private RuntaskUtils runtaskUtils;
	
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
		cmd = runtaskUtils.initAtoCommand();//初始化ATO命令数据
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
			Integer stopTime = runtaskUtils.getStopTimeCmdCurr(platformId);
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
		cmd = runtaskUtils.setNextStopStation(cmd, currStation, task);
		
		/**有扣车，设置扣车命令*/
		//cmd.setDetainCmd(getDtStatusCmd(first.getNextPlatformId()));
		
		/** 下一站有折返，设置折返命令*/
		//cmd.setTurnbackCmd(runtaskUtils.covertTurnbackCmd(first));
		
		LOG.info("--离开折返轨[getReturnLeave]--end");
		return cmd;
	}
	
	
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
		
		AppDataAVAtoCommand cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
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
				stopTime = runtaskUtils.getStopTimeCmdCurr(event.getStation());
				if(stopTime != null){
					appDataStationTiming.setTime(stopTime);
				}
			}
		}
		
		LOG.info("--appDataStationTiming--end");		
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
		
		cmd = runtaskUtils.initAtoCommand();//初始化数据
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
			Integer stopTime = runtaskUtils.getStopTimeCmdCurr(platformId);
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
		cmd = runtaskUtils.initAtoCommand();
		
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
		
		//当前车次的当前站台信息
		TrainRunTimetable currStation = runtaskUtils.getCurrStationPlan(task, platformId);
		
		/**有当前站台的信息*/
		if(currStation != null){
			cmd.setPlatformStopTime((int) currStation.getStopTime());// 站停时间（单位：秒）
			cmd.setSectionRunAdjustCmd((int) currStation.getRunTime());// 区间运行等级/区间运行时间（单位：秒）
			Integer stopTime = runtaskUtils.getStopTimeCmdCurr(platformId);
			if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
				cmd.setPlatformStopTime(stopTime);
			}
		}else{
			LOG.error("[getStaionAodCmd]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			LOG.info("--getStaionAodCmd--end");
			runtaskUtils.removeAtoCmd(event.getCargroupNum());//移除车组号对应的ATO命令缓存
			return null;
		}
			
		LOG.info("--getStaionAodCmd--end");
		runtaskUtils.updateAtoCmd(cmd);//更新车组号对应的ATO命令
		return cmd;
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
			cmd.setDetainCmd(runtaskUtils.getDtStatusCmd(platformId));
			/*if(cmd.getDetainCmd() == 0x55){//有扣车,发送扣车命令
				//cmd.setPlatformStopTime(0xFFFF);
				return cmd;
			}
			else{//判断当前车站是否有跳停
				cmd = getStationSkip(task, event, platformId);
			}*/
		} catch (Exception e) {
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
			TrainRunTimetable currStation = runtaskUtils.getCurrStationPlan(task, platformId);// 当前车次的当前站台信息
			
			if(currStation != null){
				/** 下一站有人工跳停命令或者计划有跳停,设置跳停命令、跳停站台ID
				 *  无跳停：设置无跳停命令、下一停车站台ID */
				/**从运行控制模块获取站台跳停状态信息*/
				String skipStatusStr = runtaskUtils.getSkipStationStatus(platformId);
				LOG.info("[StaionSkipStatus] platformId:{} skipStatus:{}", platformId, skipStatusStr);
				if(skipStatusStr != null && skipStatusStr.equals("error")){
					return null;
				}
				//if(currStation.getPlatformId() != 6 && skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
				//		|| currStation.isSkip()){//运行计划有跳停
				if(skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
						|| currStation.isSkip()){//运行计划有跳停
					cmd.setNextSkipCmd((short) 0x55);
					cmd.setSkipPlatformId(platformId);
				}
				//cmd = runtaskUtils.nextStaionSkipStatusProccess(cmd, currStation);
				
				/**(下一站有跳停)设置下一停车站台ID、停站时间*/
				cmd = runtaskUtils.setNextStopStation(cmd, currStation, task);
				if(cmd == null){
					return null;
				}
				if(cmd.getNextSkipCmd() == 0x55){
					Integer stopTime = runtaskUtils.getStopTimeCmdCurr(cmd.getNextStopPlatformId());
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
			TrainRunTimetable currStation = runtaskUtils.getCurrStationPlan(task, event.getStation());// 当前车次的当前站台信息
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
			MyExceptionUtil.printTrace2logger(e);
			cmd = null;
		}
		
		LOG.info("--[getStationLeave]--end");
		return cmd;
	}
	
	private AppDataAVAtoCommand getExistAtoCmdPlan(TrainRunTask task, TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
		if(cmd == null || cmd.getCargroupNum() != event.getCargroupNum()){
			getStaionAodCmd(task, event, platformId);
			cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
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
