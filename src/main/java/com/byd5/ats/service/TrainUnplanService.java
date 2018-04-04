package com.byd5.ats.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.utils.MyExceptionUtil;
import com.byd5.ats.utils.RuntaskUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TrainUnplanService {
	private static final Logger LOG = LoggerFactory.getLogger(TrainUnplanService.class);

	@Autowired
	private RuntaskUtils runtaskUtils;
	
	
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
		
		AppDataAVAtoCommand cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
		if(cmd != null && cmd.getNextStopPlatformId() == event.getStation()){
			appDataStationTiming.setTime(cmd.getPlatformStopTime());
		}
		else{
			Integer stopTime = runtaskUtils.getStopTimeCmdCurr(event.getStation());//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			if(stopTime != null){
				appDataStationTiming.setTime(stopTime);
			}
			else{
				stopTime = runtaskUtils.getDefDwellTime(event.getStation());
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
		cmd = runtaskUtils.initAtoCommand();
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setCargroupLineNum(event.getCargroupLineNum());
		cmd.setCargroupNum(event.getCargroupNum());
		cmd.setTrainNum(event.getTrainNum());
		/**设置目的地号为终点站站台ID*/
		cmd.setDstCode(runtaskUtils.convertDstCode2Char(event.getDstCode()));
		cmd.setPlanDir((short) event.getTrainDir()); // ??? need rungraph supply!
		/** 下一站区间运行时间不会改变，除非有调整*/
		Integer runtime = runtaskUtils.getDefRunTime(platformId);
		if(runtime == null){//获取失败，不下发ATO命令
			return null;
		}
		cmd.setSectionRunAdjustCmd(runtime);
		/**设置默认停车站台ID*/
		cmd.setNextStopPlatformId(platformId);
		Integer stopTime = runtaskUtils.getStopTimeCmdCurr(platformId);
		if(stopTime != null){//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			cmd.setPlatformStopTime(stopTime);
		}
		else{//车站默认停站时间
			Integer dwelltime = runtaskUtils.getDefDwellTime(platformId);
			if(dwelltime == null){
				return null;
			}
			cmd.setPlatformStopTime(dwelltime);
		}
		
		//更新ATO命令
		runtaskUtils.updateAtoCmd(cmd);
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
		cmd.setDetainCmd(runtaskUtils.getDtStatusCmd(platformId));
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
			String skipStatusStr = runtaskUtils.getSkipStationStatus(platformId);
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
		AppDataAVAtoCommand cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
		if(cmd == null || cmd.getCargroupNum() != event.getCargroupNum()){
			getStationUnplan(event, platformId);
			cmd = runtaskUtils.mapAtoCmd.get(event.getCargroupNum());
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
	
}
