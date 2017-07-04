package com.byd5.ats.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.byd5.ats.message.AppDataStationTiming;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppProtocolConstant;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.byd5.ats.protocol.ats_vobc.AppDataATOCommand;
import com.byd5.ats.protocol.ats_vobc.AppDataATOStatus;
import com.byd5.ats.protocol.ats_vobc.FrameATOCommand;
import com.byd5.ats.protocol.ats_vobc.FrameATOStatus;

/**
 * 列车运行任务处理类
 * @author hehg
 *
 */
@Component
public class RunTaskService {

	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainRunTask> mapRunTask = new HashMap<Integer, TrainRunTask>();

	/**
	 * 列车ATO状态map：以车组号为key，FrameATOStatus类为value
	 */
	public Map<Integer, FrameATOStatus> mapATOStatus = new HashMap<Integer, FrameATOStatus>();
	
	//public List<TrainRunTask> runTaskList = new ArrayList<TrainRunTask>();
	//public FrameATOStatus frameATOStatus = new FrameATOStatus();
	/**
	 * 当列车到达转换轨时，收到运行图发来的车次时刻表后，根据车次时刻表向VOBC发送任务命令
	 * @param task
	 * @return
	 */
	public FrameATOCommand frameATOCommandTask(TrainRunTask task) throws Exception {
		FrameATOCommand fCommand = new FrameATOCommand();
		AppDataATOCommand cmd = new AppDataATOCommand();

		FrameATOStatus fStatus = new FrameATOStatus();
		Integer carNum = task.getTraingroupnum();
		if (mapATOStatus.containsKey(carNum)) {
			fStatus = mapATOStatus.get(carNum);
		}
		
		AppProtocolHeader fhdr = fStatus.getFrameHeader();
		AppDataHeader mhdr = fStatus.getMsgHeader();
		AppDataATOStatus status = fStatus.getAtoStatus();
		int dstId = fhdr.getDstId();
		int srcId = fhdr.getSrcId();
		fhdr.setDstId(srcId);
		fhdr.setSrcId(dstId);
		fCommand.setFrameHeader(fhdr);
		
		mhdr.setType(AppProtocolConstant.MSGTYPE_VOBC_ATO_COMMAND);
		mhdr.setLength((short) (48+2));
		fCommand.setMsgHeader(mhdr);
		
		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum((short) 1); // ??? need rungraph supply!
		cmd.setNextZcId(0xffffffff);
		cmd.setNextCiId(0xffffffff);
		cmd.setNextAtsId(0xffffffff);
		cmd.setCarLineNum(status.getCarLineNum());
		cmd.setCarNum(status.getCarNum());
		cmd.setSrcLineNum((short) 1); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum((short) 1); // ??? need rungraph supply!
		
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable first = timetableList.get(0);
		//TrainRunTimetable last = timetableList.get(timetableList.size()-1);
		
		cmd.setDstStationNum(task.getDstStationNum());
		cmd.setDirectionPlan(task.getRunDirection()); // ??? need rungraph supply!
		
		cmd.setSkipStationId(0xFFFF);
		
		cmd.setNextStationId(first.getPlatformId());
		cmd.setStationStopTime((int) (first.getPlanLeaveTime() - first.getPlanArriveTime())/1000); //计划站停时间（单位：秒）
		
		cmd.setSkipNextStation((short) 0xFF);
		cmd.setSectionRunLevel(2);
		cmd.setDetainCmd((short) 0xFF);
		cmd.setReturnCmd((short) 0xFF);
		cmd.setGotoRailYard((short) 0xFF);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		fCommand.setAtoCommand(cmd);
		
		return fCommand;
	}
	
	/**
	 * 当列车到站停稳后，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向VOBC发送下一区间运行命令：设置区间运行等级，下一站台
	 * @param event
	 * @return
	 */
	public FrameATOCommand frameATOCommandArrive(TrainRunTask task, TrainEventPosition event) {
		FrameATOCommand fCommand = new FrameATOCommand();
		AppDataATOCommand cmd = new AppDataATOCommand();

		int platformId = event.getStation();
		//TrainRunTask task = new TrainRunTask();
		FrameATOStatus fStatus = new FrameATOStatus();
		
		Integer carNum = (int) event.getCarNum();
		/*if (mapRunTask.containsKey(carNum)) {
			task = mapRunTask.get(carNum);
		}
		else {
			// TODO
		}*/
		
		//Integer carNum = event.getTrain_num();
		if (mapATOStatus.containsKey(carNum)) {
			fStatus = mapATOStatus.get(carNum);
		}
		else {
			// TODO
		}

		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable last = timetableList.get(timetableList.size()-1);
		if (platformId == last.getPlatformId()) {
			// 到达本车次的终点站，根据车次时刻表向VOBC发送折返命令
			// TODO
		}

		TrainRunTimetable currStation = null;
		TrainRunTimetable nextStation = null;
		for (int i = 0; i < timetableList.size()-1; i ++) {
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
			}
		}
		int timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000);
		
		AppProtocolHeader fhdr = fStatus.getFrameHeader();
		AppDataHeader mhdr = fStatus.getMsgHeader();
		AppDataATOStatus status = fStatus.getAtoStatus();
		int dstId = fhdr.getDstId();
		int srcId = fhdr.getSrcId();
		fhdr.setDstId(srcId);
		fhdr.setSrcId(dstId);
		fCommand.setFrameHeader(fhdr);
		
		mhdr.setType(AppProtocolConstant.MSGTYPE_VOBC_ATO_COMMAND);
		mhdr.setLength((short) (48+2));
		fCommand.setMsgHeader(mhdr);
		
		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum((short) 1); // ??? need rungraph supply!
		cmd.setNextZcId(0xffffffff);
		cmd.setNextCiId(0xffffffff);
		cmd.setNextAtsId(0xffffffff);
		cmd.setCarLineNum(status.getCarLineNum());
		cmd.setCarNum(status.getCarNum());
		cmd.setSrcLineNum((short) 1); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum((short) 1); // ??? need rungraph supply!
		
		cmd.setDstStationNum(last.getPlatformId());
		cmd.setDirectionPlan((short) 0x55); // ??? need rungraph supply!
		
		cmd.setSkipStationId(0xFFFF);
		
		cmd.setNextStationId(currStation.getNextPlatformId());
		cmd.setStationStopTime(0xffff); //计划站停时间（单位：秒）
		
		cmd.setSkipNextStation((short) 0xFF);
		// 区间运行等级/区间运行时间
		cmd.setSectionRunLevel(timeSectionRun);
		//cmd.setSectionRunLevel(2);
		
		cmd.setDetainCmd((short) 0xFF);
		cmd.setReturnCmd((short) 0xFF);
		cmd.setGotoRailYard((short) 0xFF);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		fCommand.setAtoCommand(cmd);
		
		return fCommand;
	}
	
	/**
	 * 当列车离站时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向VOBC发送下一站台操作命令：跳停/站停（设置站停时间）
	 * @param event
	 * @return
	 */
	public FrameATOCommand frameATOCommandLeave(TrainRunTask task, TrainEventPosition event) {
		FrameATOCommand fCommand = new FrameATOCommand();
		AppDataATOCommand cmd = new AppDataATOCommand();

		int platformId = event.getStation();
		//TrainRunTask task = new TrainRunTask();
		FrameATOStatus fStatus = new FrameATOStatus();
		
		Integer carNum = (int) event.getCarNum();
		/*if (mapRunTask.containsKey(carNum)) {
			task = mapRunTask.get(carNum);
		}
		else {
			// TODO
		}*/
		
		//Integer carNum = event.getTrain_num();
		if (mapATOStatus.containsKey(carNum)) {
			fStatus = mapATOStatus.get(carNum);
		}
		else {
			// TODO
		}

		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable last = timetableList.get(timetableList.size()-1);
/*		if (platformId == last.getPlatformId()) {
			// 到达本车次的终点站，根据车次时刻表向VOBC发送折返命令
			// TODO
		}*/

		TrainRunTimetable currStation = null;
		TrainRunTimetable nextStation = null;
		for (int i = 0; i < timetableList.size()-1; i ++) {
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
			}
		}
		int timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
		int timeStationStop = (int) ((nextStation.getPlanLeaveTime() - nextStation.getPlanArriveTime())/1000); // 站停时间（单位：秒）
		
		AppProtocolHeader fhdr = fStatus.getFrameHeader();
		AppDataHeader mhdr = fStatus.getMsgHeader();
		AppDataATOStatus status = fStatus.getAtoStatus();
		int dstId = fhdr.getDstId();
		int srcId = fhdr.getSrcId();
		fhdr.setDstId(srcId);
		fhdr.setSrcId(dstId);
		fCommand.setFrameHeader(fhdr);
		
		mhdr.setType(AppProtocolConstant.MSGTYPE_VOBC_ATO_COMMAND);
		mhdr.setLength((short) (48+2));
		fCommand.setMsgHeader(mhdr);
		
		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum((short) 1); // ??? need rungraph supply!
		cmd.setNextZcId(0xffffffff);
		cmd.setNextCiId(0xffffffff);
		cmd.setNextAtsId(0xffffffff);
		cmd.setCarLineNum(status.getCarLineNum());
		cmd.setCarNum(status.getCarNum());
		cmd.setSrcLineNum((short) 1); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum((short) 1); // ??? need rungraph supply!
		
		cmd.setDstStationNum(last.getPlatformId());
		cmd.setDirectionPlan((short) 0x55); // ??? need rungraph supply!
		
		//cmd.setSkipStationId(0xFFFF);
		
		cmd.setNextStationId(currStation.getNextPlatformId());
		cmd.setStationStopTime(timeStationStop); //计划站停时间（单位：秒）
		
		// 判断下一站是否跳停？
		if (nextStation.isSkip()) {
			cmd.setSkipNextStation((short) 0x55);
			cmd.setSkipStationId(nextStation.getPlatformId());
		}
		else {
			cmd.setSkipStationId(0xFFFF);
			cmd.setSkipNextStation((short) 0xFF);
		}
		
		// 区间运行等级/区间运行时间
		//cmd.setSectionRunLevel(timeSectionRun);
		//cmd.setSectionRunLevel(2);
		
		cmd.setDetainCmd((short) 0xFF);
		cmd.setReturnCmd((short) 0xFF);
		cmd.setGotoRailYard((short) 0xFF);
		cmd.setDoorControl((short) 0xFF);
		cmd.setReserved(0);
		fCommand.setAtoCommand(cmd);
		
		return fCommand;
	}
	
	
	/**
	 * 当列车到站时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送站停时间
	 * @param event
	 * @return
	 */
	public AppDataStationTiming clientTimeStationStop(TrainRunTask task, TrainEventPosition event) {

		int platformId = event.getStation();
		Integer carNum = (int) event.getCarNum();

		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable last = timetableList.get(timetableList.size()-1);

		TrainRunTimetable currStation = null;
		TrainRunTimetable nextStation = null;
		for (int i = 0; i < timetableList.size()-1; i ++) {
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
			}
		}
		int timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
		int timeStationStop = (int) ((nextStation.getPlanLeaveTime() - nextStation.getPlanArriveTime())/1000); // 站停时间（单位：秒）
		
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		appDataStationTiming.setStation_id(currStation.getNextPlatformId());
		appDataStationTiming.setTime(timeStationStop); //计划站停时间（单位：秒）
		
		return appDataStationTiming;
	}
}
