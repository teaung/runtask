package com.byd5.ats.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 列车运行任务处理类
 * @author hehg
 *
 */
@Component
public class RunTaskService {
	private static final Logger LOG = LoggerFactory.getLogger(RunTaskService.class);

	@Autowired
	private TraincontrolHystrixService traincontrolHystrixService;
	
	/**
	 * 运行任务map：以车组号为key，TrainRunTask类为value
	 */
	public Map<Integer, TrainRunTask> mapRunTask = new HashMap<Integer, TrainRunTask>();

	/**
	 * 运行任务map：以车组号为key，TrainTrace类为value
	 */
	public Map<Integer, TrainEventPosition> mapTrace = new HashMap<Integer, TrainEventPosition>();
	
	/**
	 * 设置停站时间map：以站台ID为key，AppDataDwellTimeCommand类为value
	 */
	public Map<Integer, AppDataDwellTimeCommand> mapDwellTime = new HashMap<Integer, AppDataDwellTimeCommand>();
	
	/**
	 * 当列车到达折返时，收到运行图发来的车次时刻表后，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param task
	 * @return
	 */
	public AppDataAVAtoCommand aodCmdReturn(TrainRunTask task) throws Exception {
		LOG.info("--达折返aodCmdReturn--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		cmd.setServiceNum((short) task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setNextZcId(0);//0xffffffff
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum((short) task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		TrainRunTimetable first = timetableList.get(1);//第二条数据为车站数据
		
		TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
		String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
		
//		cmd.setDstCode(endPlatformId);
		cmd.setDstCode(lastStation.getPlatformId());
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		//cmd.setNextStationId(first.getPlatformId());//下一停车站台ID
		cmd.setNextStopPlatformId(0xFFFF);//下一停车站台ID(默认)
		cmd.setPlatformStopTime(0xFFFF); //计划站停时间（单位：秒）
		cmd.setSkipPlatformId(0xFFFF);
		cmd.setNextSkipCmd((short) 0xAA);
		cmd.setSectionRunAdjustCmd((short) 0);//?
		
		//下一站有人工设置跳停命令或者运行计划有跳停
		String skipStatusStr = null;
		skipStatusStr = traincontrolHystrixService.getSkipStationStatus(first.getPlatformId());
		if(skipStatusStr != null && skipStatusStr.equals("1") || first.isSkip()){//有跳停
			cmd.setSkipPlatformId(first.getPlatformId());
			cmd.setNextSkipCmd((short) 0x55);
		}
		else {//下一站无跳停
			cmd.setSkipPlatformId(0xFFFF);
			cmd.setNextSkipCmd((short) 0xAA);
			cmd.setNextStopPlatformId(first.getPlatformId());
		}			
		
		if(cmd.getNextSkipCmd() == 0x55){//有跳停,则获取跳停站台后的第一个停车站台
			int platformId = first.getNextPlatformId();
			int runtime = 0;
			for (int i = 0; i < timetableList.size()-1; i ++) {//获取下一停车站台ID
				TrainRunTimetable t = timetableList.get(i);
				//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
				if (t.getPlatformId() == platformId && platformId!= 0 && platformId != 9){
					String skipStatusStr1 = traincontrolHystrixService.getSkipStationStatus(platformId);
					runtime += (int) ((t.getPlanArriveTime() - first.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
					if(!t.isSkip() && !(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {
						cmd.setNextStopPlatformId(t.getPlatformId());
						//cmd.setSectionRunLevel(runtime);
						break;
					}
					platformId = t.getNextPlatformId();
					first = timetableList.get(i);
				}
				
			}
			
		}
		
		cmd.setDetainCmd((short) 0);
		cmd.setTurnbackCmd((short) 0);
		cmd.setBackDepotCmd((short) 0);
		cmd.setDoorctrlStrategy((short) 0xFF);
		cmd.setReserved(0);
		LOG.info("--达折返aodCmdReturn--end");
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
	public AppDataAVAtoCommand aodCmdEnter(TrainRunTask task, TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {
		LOG.info("--aodCmdEnter--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		int platformId = event.getStation();

		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		TrainRunTimetable currStation = null;
		TrainRunTimetable nextStation = null;
		int timeSectionRun = 0;	//下一站区间运行时间
		int timeStationStop = 0; // 当前车站站停时间（单位：秒）
		for (int i = 0; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
			}
		}
		if(currStation != null && nextStation != null){
			timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）

			cmd.setServiceNum((short) task.getTablenum());
			cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
			cmd.setNextZcId(0);
			cmd.setNextCiId(0);
			cmd.setNextAtsId(0);
			cmd.setCargroupLineNum(task.getLineNum());
			cmd.setCargroupNum((short) task.getTraingroupnum());
			cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
			cmd.setTrainNum((short) task.getTrainnum());
			cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
			
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
			String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
			
			//cmd.setDstStationNum(endPlatformId);
			cmd.setDstCode(lastStation.getPlatformId());
			cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
			
			//若当前车站是终点站，则只发当前车站站停时间
			if(currStation.getPlatformId() == lastStation.getPlatformId()){
				cmd.setSkipPlatformId(0xFFFF);
				cmd.setNextSkipCmd((short) 0xAA);
				cmd.setNextStopPlatformId(0xFFFF);
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
				cmd.setSectionRunAdjustCmd((short) 0);
				//cmd.setSectionRunLevel(timeSectionRun);
			}
			//若当前车站不是终点站，则发当前车站站停时间，下一站台ID，下一站区间运行时间
			else{
				//cmd.setNextStationId(currStation.getNextPlatformId());
				cmd.setNextStopPlatformId(0xFFFF);
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
				
				//下一站有人工设置跳停命令或者运行计划有跳停
				String skipStatusStr = null;
				skipStatusStr = traincontrolHystrixService.getSkipStationStatus(nextStation.getPlatformId());
				if(skipStatusStr != null && skipStatusStr.equals("1") || nextStation.isSkip()){//有跳停
					cmd.setSkipPlatformId(nextStation.getPlatformId());
					cmd.setNextSkipCmd((short) 0x55);
				}
				else {//下一站无跳停
					cmd.setSkipPlatformId(0xFFFF);
					cmd.setNextSkipCmd((short) 0xAA);
					cmd.setNextStopPlatformId(currStation.getNextPlatformId());
					cmd.setSectionRunAdjustCmd((short) timeSectionRun);// 区间运行等级/区间运行时间
				}				
			
			}
			
			cmd.setDetainCmd((short) 0);
			cmd.setTurnbackCmd((short) 0);
			cmd.setBackDepotCmd((short) 0);
			cmd.setDoorctrlStrategy((short) 0xFF);
			cmd.setReserved(0);
			
			//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			if(mapDwellTime.containsKey(currStation.getPlatformId())){
				AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(currStation.getPlatformId());
				if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
					timeStationStop = dwellTimeCommand.getTime();//设置停站时间
					cmd.setPlatformStopTime(timeStationStop);
				}
			}		
				
			//判断当前车站是否人工设置跳停命令
			String skipStatus = null;
			skipStatus = traincontrolHystrixService.getSkipStationStatus(currStation.getPlatformId());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				cmd.setPlatformStopTime(0x0001); //计划站停时间（单位：秒）
			}
			
			if(cmd.getNextSkipCmd() == 0x55){//有跳停,则获取跳停站台后的第一个停车站台
				int nextPlatformId = currStation.getNextPlatformId();
				int runtime = 0;
				for (int i = 0; i < timetableList.size()-1; i ++) {//获取下一停车站台ID
					TrainRunTimetable t = timetableList.get(i);
					
					if (t.getPlatformId() == nextPlatformId  && platformId!= 0 && platformId != 9){
						String skipStatusStr1 = traincontrolHystrixService.getSkipStationStatus(nextPlatformId);
						System.out.println("------skipStatusStr1---"+skipStatusStr1);
						System.out.println(t.getPlatformId()+"------nextPlatformId---"+nextPlatformId);
						runtime += (int) ((t.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
						if(!t.isSkip() && !(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
							cmd.setNextStopPlatformId(t.getPlatformId());
							cmd.setSectionRunAdjustCmd((short) runtime);
							break;
						}
						nextPlatformId = t.getNextPlatformId();
						currStation = timetableList.get(i);
					}
				}
			}
		}
		else{
			LOG.error("[aodCmdEnter]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			return null;
		}
		
		
		LOG.info("--aodCmdEnter--end");		
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
		LOG.info("--appDataStationTiming--start");
		int platformId = event.getStation();
		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		TrainRunTimetable currStation = null;
		for (int i = 0; i < timetableList.size(); i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
			}
		}
		
		if(currStation != null){
			int timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			
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
			skipStatus = traincontrolHystrixService.getSkipStationStatus(currStation.getPlatformId());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				appDataStationTiming.setTime(0x0001);; //设置站停时间（单位：秒）
			}
		}
		else{
			LOG.error("[appDataStationTiming]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			return null;
		}
		
		LOG.info("--appDataStationTiming--end");		
		return appDataStationTiming;
	}
	
	
	/**
	 * (非计划车)当列车到站停稳时，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向客户端发送列车站停时间
	 * @param event
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public AppDataStationTiming appDataStationTimingUnplan(TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {
		LOG.info("--appDataStationTimingUnplan--start");
		Integer platformId = event.getStation();

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
		
		/*if(event.getStation() != null){
			//判断是否人工设置跳停命令
			String skipStatus = null;
			skipStatus = traincontrolHystrixService.getSkipStationStatus(platformId);
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				appDataStationTiming.setTime(0x0001);; //设置站停时间（单位：秒）
			}
		}*/
		
		
		/*try{
			skipStatus = restTemplate.getForObject("http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}", String.class, platformId);
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				appDataStationTiming.setTime(0x0001);; //设置站停时间（单位：秒）
			}
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("serv35-traincontrol can't connect, or parse error!");
			//e.printStackTrace();
		}*/
		
		LOG.info("--appDataStationTimingUnplan--end");		
		return appDataStationTiming;
	}
	
	
	/**
	 * (非计划车)当列车到站(不管是否停稳)后，收到识别跟踪发来的列车位置报告事件后，根据车次时刻表向VOBC发送当前车站(默认)站停时间
	 * @param event
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public AppDataAVAtoCommand aodCmdEnterUnplan(TrainEventPosition event) throws JsonParseException, JsonMappingException, IOException {
		LOG.info("--aodCmdEnterUnplan--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		int timeStationStop = 30; // 当前车站站停时间（单位：秒）
		
		cmd.setServiceNum((short) 0xFF);
		cmd.setLineNum((short) 64); // ??? need rungraph supply!
		cmd.setNextZcId(0);
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCargroupLineNum((short) 64);
		cmd.setCargroupNum(event.getCarNum());
		cmd.setSrcLineNum((short) 64); // ??? need rungraph supply!
		cmd.setTrainNum((short) 0000);
		cmd.setDstLineNum((short) 64); // ??? need rungraph supply!
		
//		cmd.setDstCode(String.valueOf(0xFFFF));
		cmd.setDstCode(0xFFFF);
		cmd.setPlanDir((short) ((event.getDirectionPlan()==0)?0xAA:0x55)); 
		
		cmd.setSkipPlatformId(0xFFFF);
		cmd.setNextSkipCmd((short) 0xAA);
		//cmd.setNextStationId(0xFFFF);
		cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）默认30s
		cmd.setSectionRunAdjustCmd((short) 0);//(0xFFFF);
		
		cmd.setDetainCmd((short) 0);
		cmd.setTurnbackCmd((short) 0);
		cmd.setBackDepotCmd((short) 0);
		cmd.setDoorctrlStrategy((short) 0xFF);
		cmd.setReserved(0);
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		if(mapDwellTime.containsKey(event.getStation())){
			AppDataDwellTimeCommand dwellTimeCommand = mapDwellTime.get(event.getStation());
			if(dwellTimeCommand.getSetWay() == 0){//0为人工设置
				timeStationStop = dwellTimeCommand.getTime();//设置停站时间
				cmd.setPlatformStopTime(timeStationStop);
			}
		}		
		
		if(event.getStation() == null){//折返轨
			cmd.setPlatformStopTime(0xFFFF);
		}
		
		/*Integer nextPlatformId = event.getStation() + 1;
		if(event.getStation().equals(8)){
			nextPlatformId = 1;
		}*/
		Integer nextPlatformId = event.getNextStationId();
		cmd.setNextStopPlatformId(nextPlatformId);
		
		//判断下一站是否人工设置跳停命令
		/*String skipStatusStr = null;
		skipStatusStr = traincontrolHystrixService.getSkipStationStatus(nextPlatformId);
		if(skipStatusStr != null && skipStatusStr.equals("1")){//有跳停
			cmd.setSkipStationId(nextPlatformId);
			cmd.setSkipNextStation((short) 0x55);
		}
		
		if(event.getStation() != null){
			//判断当前车站是否人工设置跳停命令
			String skipStatus = null;
			skipStatus = traincontrolHystrixService.getSkipStationStatus(event.getStation());
			if(skipStatus != null && skipStatus.equals("1")){//有跳停
				cmd.setStationStopTime(0x0001); //计划站停时间（单位：秒）
			}
		}*/
		LOG.info("--aodCmdEnterUnplan--end");
		return cmd;
	}
	
	
	
	/**
	 * 当列车到达转换轨时，收到运行图发来的运行信息，根据车次时刻表向VOBC发送任务命令（表号、车组号、车次号信息）
	 * @param task
	 * @return
	 */
	public AppDataAVAtoCommand aodCmdTransform(TrainRunInfo trainRunInfo) throws Exception {
		LOG.info("--aodCmdTransform--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		cmd.setServiceNum((short) trainRunInfo.getTablenum());
		cmd.setLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setNextZcId(0);//0xffffffff
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCargroupLineNum((short) trainRunInfo.getLineNum());
		cmd.setCargroupNum((short) trainRunInfo.getTraingroupnum());
		cmd.setSrcLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) trainRunInfo.getTrainnum());
		cmd.setDstLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		
		cmd.setDstCode(0);	//填啥？
		//cmd.setDstStationNum(task.getDstStationNum());
		cmd.setPlanDir((short) ((trainRunInfo.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		cmd.setNextStopPlatformId(0xFFFF);
		cmd.setPlatformStopTime(0xFFFF); //计划站停时间（单位：秒）
		cmd.setSkipPlatformId(0xFFFF);
		cmd.setNextSkipCmd((short) 0xAA);
		cmd.setSectionRunAdjustCmd((short) 0);//?
		
		cmd.setDetainCmd((short) 0);
		cmd.setTurnbackCmd((short) 0);
		cmd.setBackDepotCmd((short) 0);
		cmd.setDoorctrlStrategy((short) 0xFF);
		cmd.setReserved(0);
		LOG.info("--aodCmdTransform--end");
		return cmd;
	}

	/**更新运行任务列表*/
	public void updateMapRuntask(Integer carNum, TrainRunTask runTask){
		if (!mapRunTask.containsKey(carNum)) {
			mapRunTask.put(carNum, runTask);
		}
		else {
			mapRunTask.replace(carNum, runTask);
		}
	}
	
	/**更新列车位置信息列表*/
	public void updateMapTrace(Integer carNum, TrainEventPosition trace){
		if (!mapTrace.containsKey(carNum)) {
			mapTrace.put(carNum, trace);
		}
		else {
			mapTrace.replace(carNum, trace);
		}
	}
	
	/**检查该车是否有记录*/
	public TrainEventPosition getMapTrace(Integer carNum){
		if (mapTrace.containsKey(carNum)) {
			return mapTrace.get(carNum);
		}
		return null;		
	}
	
	/**检查该运行任务是否有记录*/
	public TrainRunTask getMapRuntask(Integer carNum){
		if (mapRunTask.containsKey(carNum)) {
			return mapRunTask.get(carNum);
		}
		return null;		
	}
	
	/**非计划车时，移除残留的计划车运行任务信息*/
	public void clearMapRuntask(Integer carNum, short tablenum){
		if (tablenum == 0) {//非计划车，则移除
			mapRunTask.remove(carNum);
		}
	}
}
