package com.byd5.ats.service;

import java.util.ArrayList;
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
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
import com.byd5.ats.utils.RuntaskConstant;
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
	
	/**初始化扣车状态数组大小，默认所有车站扣车状态为未扣车(3)*/
	public RunTaskService()
	{
		for(int i=0;i<8;i++)
		{
			this.listDtStatus.add((byte) 3);
		}
		
	}
	
	/**
	 * 当列车到达折返时，收到运行图发来的车次时刻表后，根据车次时刻表向VOBC发送任务命令（新的车次号、下一站ID）
	 * @param event 列车位置信息
	 * @param task	运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand aodCmdReturn(TrainEventPosition event, TrainRunTask task) {
		LOG.info("--达折返aodCmdReturn--start");
		/**ATO命令信息*/
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		/**初始化ATO命令数据为默认值*/
		cmd = initAtoCommand(cmd);//初始化ATO命令数据
		
		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum(task.getTablenum());
		cmd.setLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum(task.getLineNum());
		cmd.setCargroupNum(task.getTraingroupnum());
		cmd.setSrcLineNum(task.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum(task.getTrainnum());
		cmd.setDstLineNum(task.getLineNum()); // ??? need rungraph supply!
		
		/**当前车次对应的时刻表信息*/
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();
		/**当前车次的起始站信息*/
		TrainRunTimetable first = timetableList.get(1);//第二条数据为车站数据
		/**当前车次的终点站信息*/
		TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
		
		/**设置目的地号为终点站站台ID*/
		String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
		cmd = setDstCode(task.getDstStationNum(), cmd);
		cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		/**下一站跳停状态处理： 设置跳停命令、下一跳停站台ID，应满足以下条件： 1、有人工设置跳停命令 2、或者运行计划有跳停*/
		cmd = nextStaionSkipStatusProccess(cmd, first);
		
		/**(下一站有跳停)设置下一停车站台ID、区间运行时间*/
		cmd = setNextStopStation(cmd, first, timetableList);
		
		/**有扣车，设置扣车命令*/
		cmd = setDtCmd(cmd);
		
		LOG.info("--到达折返轨aodCmdReturn--end");
		return cmd;
	}

	/**
	 * 设置目的地号: 类型转换String->char[]
	 * 
	 * @param dstStationNum 运行图目的地号
	 * @param cmd ATO命令信息
	 * @return ATO命令信息
	 */
	private AppDataAVAtoCommand setDstCode(String dstStationNum, AppDataAVAtoCommand cmd) {
		int len = 4;
		char[] code = {' ', ' ', ' ', ' '};
		char[] dst = dstStationNum.toCharArray();
		if (dst.length < len) {
			len = dst.length;
		}
		for (int i = 0; i < len; i ++) {
			code[3-i] = dst[len-1-i];
		}
		cmd.setDstCode(code);
		return cmd;
	}

	/**
	 * 下一停车站台有扣车，设置有扣车命令
	 * @param cmd ATO命令信息
	 * @return ATO命令信息
	 */
	private AppDataAVAtoCommand setDtCmd(AppDataAVAtoCommand cmd) {
		/**下一停车站台ID有扣车3，则设置有扣车命令0x55*/
		if(cmd.getNextStopPlatformId() <= 8 && listDtStatus.get(cmd.getNextStopPlatformId()-1) < 3){
			cmd.setDetainCmd((short) 0x55);
		}
		return cmd;
	}
	
	/**
	 *  当列车到站(不管是否停稳)，根据列车位置、车次时刻表向VOBC发送下一区间运行命令：设置区间运行等级
	 * @param task 运行任务
	 * @param event 列车位置
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand aodCmdEnter(TrainRunTask task, TrainEventPosition event) {
		LOG.info("--aodCmdEnter--start");
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
		
		int platformId = event.getStation();

		/**当前车次对应的时刻表信息*/
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		/**当前车次的当前站台信息*/
		TrainRunTimetable currStation = null;
		
		/**当前车次的下一站台信息*/
		TrainRunTimetable nextStation = null;
		
		int timeSectionRun = 0;	//下一站区间运行时间
		int timeStationStop = 0; // 当前车站站停时间（单位：秒）
		
		/**获取当前车次时刻表的对应当前站台、下一站台的信息*/
		for (int i = 0; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
			TrainRunTimetable t = timetableList.get(i);
			if (t.getPlatformId() == platformId) {
				currStation = t;
				nextStation = timetableList.get(i+1);
				break;
			}
		}
		
		/**有当前站台、下一站台的信息*/
		if(currStation != null && nextStation != null){
			timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
			timeSectionRun = (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
			/**当前车次的终点站信息*/
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 2);//最后一条数据为车站数据
			
			/**设置目的地号为终点站站台ID*/
			String endPlatformId = String.valueOf(lastStation.getPlatformId());//终点站站台ID
			cmd = setDstCode(task.getDstStationNum(), cmd);
			//cmd.setDstCode(endPlatformId.toCharArray());
			cmd.setPlanDir((short) ((task.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
			
			/**若当前车站是终点站，则只发当前车站站停时间*/
			if(currStation.getPlatformId() == lastStation.getPlatformId()){
				cmd.setNextSkipCmd((short) 0xAA);
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
			}
			/**若当前车站不是终点站，则发当前车站站停时间，下一站台ID，下一站区间运行时间*/
			else{
				cmd.setPlatformStopTime(timeStationStop); //计划站停时间（单位：秒）
				cmd.setSectionRunAdjustCmd((short) timeSectionRun);// 区间运行等级/区间运行时间
				
				/**下一站有人工设置跳停命令或者运行计划有跳停,设置跳停命令、跳停站台ID*/
				cmd = nextStaionSkipStatusProccess(cmd, nextStation);		
				
				/**(下一站有跳停)设置下一停车站台ID、区间运行时间*/
				cmd = setNextStopStation(cmd, currStation, timetableList);
			}
			
			/**1、先判断当前车站是否人工设置跳停命令*/
			Integer stopTime = getStopTimeIfSkipCurr(platformId);
			if(stopTime != null){//有跳停
				cmd.setPlatformStopTime(stopTime);
			}
			/**2、否则如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间*/
			else{//
				stopTime = getStopTimeCmdCurr(platformId);
				if(stopTime != null){
					cmd.setPlatformStopTime(stopTime);
				}
			}
			
			/*//(下一站有跳停)设置下一停车站台ID、区间运行时间
			cmd = setNextStopStation(cmd, currStation, timetableList);*/
		}
		else{
			LOG.error("[aodCmdEnter]--this trainnum {} doesn't have plan in this station {}, so discard", event.getTrainNum(),event.getStation());
			return null;
		}
		
		/**下一停车站台有扣车，设置有扣车命令*/
		cmd = setDtCmd(cmd);
		
		LOG.info("--aodCmdEnter--end");		
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
		List<TrainRunTimetable> timetableList = task.getTrainRunTimetable();

		TrainRunTimetable currStation = null;
		for (int i = 0; i < timetableList.size(); i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
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

		int timeStationStop = (int) ((currStation.getPlanLeaveTime() - currStation.getPlanArriveTime())/1000); // 当前车站站停时间（单位：秒）
		
		appDataStationTiming.setStation_id(currStation.getPlatformId());
		appDataStationTiming.setTime(timeStationStop); //计划站停时间（单位：秒）
		
		//1、先判断当前车站是否人工设置跳停命令
		Integer stopTime = getStopTimeIfSkipCurr(platformId);
		if(stopTime != null){
			appDataStationTiming.setTime(stopTime);//设置站停时间（单位：秒）
		}
		else{//2、否则如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
			stopTime = getStopTimeCmdCurr(event.getStation());
			if(stopTime != null){
				appDataStationTiming.setTime(stopTime);
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
		Integer platformId = event.getStation();

		AppDataStationTiming appDataStationTiming = new AppDataStationTiming();
		appDataStationTiming.setStation_id(platformId);
		appDataStationTiming.setTime(RuntaskConstant.DEF_DWELL_TIME); //计划站停时间（单位：秒）
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		Integer stopTime = getStopTimeCmdCurr(event.getStation());
		if(stopTime != null){
			appDataStationTiming.setTime(stopTime);
		}
		
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
	/*public AppDataAVAtoCommand aodCmdEnterUnplan(TrainEventPosition event) {
		LOG.info("--aodCmdEnterUnplan--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();

		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum((short) 0xFF);
		cmd.setLineNum(RuntaskConstant.NID_LINE); // ??? need rungraph supply!
		cmd.setCargroupLineNum(RuntaskConstant.NID_LINE);
		cmd.setCargroupNum((short) event.getCargroupNum());
		cmd.setSrcLineNum(RuntaskConstant.NID_LINE); // ??? need rungraph supply!
		cmd.setTrainNum((short) 0000);
		cmd.setDstLineNum(RuntaskConstant.NID_LINE); // ??? need rungraph supply!
		
		cmd.setPlanDir((short) ((event.getTrainDir()==0)?0xAA:0x55)); 
		
		cmd.setNextSkipCmd((short) 0xAA);
		cmd.setPlatformStopTime(RuntaskConstant.DEF_DWELL_TIME); //计划站停时间（单位：秒）默认30s
		
		//如果人工设置了当前站台的停站时间，则将该时间作为该站台的停站时间
		Integer stopTime = isArtificialStopCurr(event.getStation());
		if(stopTime != null){
			cmd.setPlatformStopTime(stopTime);
		}
		
		Integer nextPlatformId = event.getNextStationId();
		cmd.setNextStopPlatformId(nextPlatformId);
		
		//有扣车，设置扣车命令
		cmd = setDtCmd(cmd);
		
		LOG.info("--aodCmdEnterUnplan--end");
		return cmd;
	}*/
	
	
	
	/**
	 * 当列车到达转换轨时，收到运行图发来的运行信息，根据车次时刻表向VOBC发送任务命令（表号、车组号、车次号信息）
	 * @param event 列车位置信息
	 * @param task 运行任务信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand aodCmdTransform(TrainEventPosition event, TrainRunInfo trainRunInfo){
		LOG.info("--aodCmdTransform--start");
		AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
		
		cmd = initAtoCommand(cmd);//初始化数据

		cmd.setReserved((int) event.getSrc());	//预留字段填车辆VID
		cmd.setServiceNum((short) trainRunInfo.getTablenum());
		cmd.setLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setCargroupLineNum((short) trainRunInfo.getLineNum());
		cmd.setCargroupNum((short) trainRunInfo.getTraingroupnum());
		cmd.setSrcLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		cmd.setTrainNum((short) trainRunInfo.getTrainnum());
		cmd.setDstLineNum((short) trainRunInfo.getLineNum()); // ??? need rungraph supply!
		
		//cmd.setDstCode(0);	//填啥？
		cmd = setDstCode(trainRunInfo.getDstStationNum(), cmd);
		cmd.setPlanDir((short) ((trainRunInfo.getRunDirection()==0)?0xAA:0x55)); // ??? need rungraph supply!
		
		//列车到达折返轨时，只发下一站台ID
		cmd.setNextSkipCmd((short) 0xAA);
		cmd.setSectionRunAdjustCmd((short) 0);//?
		
		//有扣车，设置扣车命令
		cmd = setDtCmd(cmd);

		LOG.info("--aodCmdTransform--end");
		return cmd;
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
				//result.setStationStopTime(dwellTimeCommand.getTime());//设置停站时间
				result = dwellTimeCommand.getTime();
			}
		}
		return result;
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
			result = 0x0001;
		}
		return result;
	}
	
	/**
	 * (若下一站有跳停)设置下一停车站台ID、区间运行时间
	 * @param cmd ATO命令信息
	 * @param currStation 当前站台信息
	 * @param timetableList 车次时刻表
	 * @return ATO 命令信息
	 */
	public AppDataAVAtoCommand setNextStopStation(AppDataAVAtoCommand cmd, TrainRunTimetable currStation, List<TrainRunTimetable> timetableList) {
		AppDataAVAtoCommand atoCmd = cmd;
		if(atoCmd.getNextSkipCmd() == 0x55  
				&& currStation.getPlatformId()!= 0 
				&& currStation.getPlatformId() != 9){//有跳停,则获取跳停站台后的第一个停车站台
			int nextPlatformId = currStation.getNextPlatformId();
			int runtime = 0; //区间运行时间
			for (int i = 0; i < timetableList.size()-1; i ++) {//获取下一停车站台ID
				TrainRunTimetable nextStation = timetableList.get(i);
				if (nextStation.getPlatformId() == nextPlatformId){
					String skipStatusStr1 = traincontrolHystrixService.getSkipStationStatus(nextPlatformId);
					//System.out.println("platformId:"+nextPlatformId+" skipStatus:"+skipStatusStr1);
					runtime += (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
					if(!nextStation.isSkip() && !(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						atoCmd.setSectionRunAdjustCmd((short) runtime);
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
	 * 下一站跳停状态处理： 设置跳停命令、下一跳停站台ID，应满足以下条件：
	 * 		1、有人工设置跳停命令
	 * 		2、或者运行计划有跳停
	 * @param cmd ATO命令信息
	 * @param nextStation 下一站台信息
	 * @return ATO命令信息
	 */
	public AppDataAVAtoCommand nextStaionSkipStatusProccess(AppDataAVAtoCommand cmd, TrainRunTimetable nextStation){
		//下一站有人工设置跳停命令或者运行计划有跳停
		AppDataAVAtoCommand atoCmd = cmd;
		/**从运行控制模块获取站台跳停状态信息*/
		String skipStatusStr = traincontrolHystrixService.getSkipStationStatus(nextStation.getPlatformId());
		if(skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
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
	 * 更新运行任务列表
	 * @param carNum 车组号
	 * @param runTask 运行任务信息
	 */
	public void updateMapRuntask(Integer carNum, TrainRunTask runTask){
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
		cmd.setServiceNum(0xFF);
		cmd.setLineNum(0);
		cmd.setNextZcId(0);;
		cmd.setNextCiId(0);
		cmd.setNextAtsId(0);
		cmd.setCargroupLineNum(0xFF);
		cmd.setCargroupNum((short) 0);
		cmd.setSrcLineNum(0xFF);
		cmd.setTrainNum((short) 0xFF);
		cmd.setDstLineNum(0xFF);
		char[] dstCode = {' ',' ',' ',' '};
		cmd.setDstCode(dstCode);//0xFFFF
		cmd.setPlanDir((short) 0xFF);
		cmd.setNextStopPlatformId(0xFFFF);
		cmd.setPlatformStopTime(0xFFFF);
		cmd.setSkipPlatformId(0xFFFF);
		cmd.setNextSkipCmd((short) 0xFF);
		cmd.setSectionRunAdjustCmd((short) 0);
		cmd.setDetainCmd((short) 0xAA);
		cmd.setTurnbackCmd((short) 0);
		cmd.setBackDepotCmd((short) 0);
		cmd.setDoorctrlStrategy((short) 0xFF);
		cmd.setReserved(0);
		return cmd;
	}
}
