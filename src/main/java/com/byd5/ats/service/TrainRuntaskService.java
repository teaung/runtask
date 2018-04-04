package com.byd5.ats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;

/**
 * 列车运行任务处理类
 * @author wu.xianglan
 *
 */
@Component
public class TrainRuntaskService{

	@Autowired
	private TrainUnplanService trainUnplanService;
	@Autowired
	private TrainPlanService trainPlanService;
	
	/**
	 * 非计划车在区间运行时发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationSectionUnplan(TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationSectionUnplan(event);
		return cmd;
	}
	
	/**
	 * 非计划车进站发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationEnterUnplan(TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationEnterUnplan(event);
		return cmd;
	}
	
	/**
	 * 非计划车在站台上时发送ATO命令(停稳时，下发的命令只有设置/取消扣车)
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationArriveUnplan(TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationArriveUnplan(event);
		return cmd;
	}
	
	/**
	 * (非计划车)当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param event 列车位置信息
	 * @return ATO命令信息
	 */
	public AppDataStationTiming appDataStationTimingUnplan(TrainEventPosition event) {
		AppDataStationTiming appDataStationTiming = trainUnplanService.appDataStationTimingUnplan(event);
		return appDataStationTiming;
	}
	
	/**
	 * 非计划车离站
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationLeaveUnplan(TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationLeaveUnplan(event);
		return cmd;
	}
	
	/**
	 * 非计划车，离站时，保存至公共map中，发当前站停站时间，下一站区间运行时间(默认无扣车跳停)
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationUnplan(TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationUnplan(event, platformId);	
		return cmd;
	}
	
	/**
	 * 获取车站扣车状态命令(非计划车)
	 * @param event
	 * @param platformId 
	 * @return
	 */
	public AppDataAVAtoCommand getStationDetainUnplan(TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationDetainUnplan(event, platformId);		
		return cmd;
		
	}
	
	/**
	 * 获取当前站台跳停状态命令(非计划车)
	 * @param event
	 * @param platformId 
	 * @return
	 */
	public AppDataAVAtoCommand getStationSkipUnplan(TrainEventPosition event, Integer platformId) {
		AppDataAVAtoCommand cmd = trainUnplanService.getStationSkipUnplan(event, platformId);	
		return cmd;
	}

	
	/**
	 * 当列车离开折返时，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param event 列车位置信息
	 * @param task	运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getReturnLeave(TrainRunTask task, TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainPlanService.getReturnLeave(task, event);
		return cmd;
	}
	
	
	/**
	 * * 当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param task 运行任务信息
	 * @param event 列车位置信息
	 * @return ATO命令信息
	 */
	public AppDataStationTiming appDataStationTiming(TrainRunTask task, TrainEventPosition event) {
		AppDataStationTiming appDataStationTiming = trainPlanService.appDataStationTiming(task, event);
		return appDataStationTiming;
	}
	
	
	/**
	 * 当列车出段到达转换轨时，根据车次时刻表向VOBC发送任务命令（表号、车组号、车次号信息）
	 * @param event 列车位置信息
	 * @param task 运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand getTransformLeave(TrainRunTask task, TrainEventPosition event){
		AppDataAVAtoCommand cmd = trainPlanService.getTransformLeave(task, event);
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
		AppDataAVAtoCommand cmd = trainPlanService.getStationDetain(task, event, platformId);	
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
		AppDataAVAtoCommand cmd = trainPlanService.getStationSkip(task, event, platformId);
		return cmd;
	}
	
	/**
	 * 计划车在区间运行时发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationSection(TrainRunTask task, TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainPlanService.getStationSection(task, event);
		return cmd;
	}
	
	/**
	 * 计划车进站发送ATO命令
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationEnter(TrainRunTask task, TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainPlanService.getStationEnter(task, event);
		return cmd;
	}
	
	/**
	 * 计划车在站台上时发送ATO命令
	 * @param task
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationArrive(TrainRunTask task, TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainPlanService.getStationArrive(task, event);
		return cmd;
	}
	
	/**计划车离站发送ATO命令
	 * @param task
	 * @param event
	 * @return
	 */
	public AppDataAVAtoCommand getStationLeave(TrainRunTask task, TrainEventPosition event) {
		AppDataAVAtoCommand cmd = trainPlanService.getStationLeave(task, event);
		return cmd;
	}
}
