package com.byd5.ats.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.service.hystrixService.TraincontrolHystrixService;
import com.byd5.ats.service.hystrixService.TrainrungraphHystrixService;
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
		
		//cmd.setDstCode(code);
		return code;
	}
	
	
	/**
	 * 当前车站人工设置跳停命令,停站时间为0
	 * @param platformId 站台ID
	 * @return 停站时间	若有跳停则停站时间为0，否则返回null
	 */
	public Integer getStopTimeIfSkipCurr(int platformId){
		Integer result = null;
		/*if(platformId == 6){
			return result;
		}*/
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
	public AppDataAVAtoCommand setNextStopStation(AppDataAVAtoCommand cmd, TrainRunTimetable currStation, List<TrainRunTimetable> timetableList) {
		AppDataAVAtoCommand atoCmd = cmd;
		if(atoCmd.getNextSkipCmd() == 0x55  
				&& currStation.getPlatformId()!= 0 
				&& currStation.getPlatformId() != 9){//有跳停,则获取跳停站台后的第一个停车站台
			int nextPlatformId = currStation.getNextPlatformId();
			int runtime = 0; //区间运行时间
			long stoptime = 0;//停站时间
//			for (int i = 1; i < timetableList.size()-1; i ++) {//获取下一停车站台ID
			for (int i = 1; i < timetableList.size(); i ++) {//获取下一停车站台ID
				TrainRunTimetable nextStation = timetableList.get(i);
				if (nextStation.getPlatformId() == nextPlatformId){
					String skipStatusStr1 = traincontrolHystrixService.getSkipStationStatus(nextPlatformId);
					System.out.println("platformId:"+nextPlatformId+" skipStatus:"+skipStatusStr1);
					if(skipStatusStr1 != null && skipStatusStr1.equals("error")){
						return null;
					}
					//runtime += (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
					if(!nextStation.isSkip() && !(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						//atoCmd.setSectionRunAdjustCmd((short) runtime);
						//stoptime = (nextStation.getPlanLeaveTime() - nextStation.getPlanArriveTime())/1000;
						atoCmd.setPlatformStopTime((int) nextStation.getStopTime());
						break;
					}
					if(nextPlatformId == 6 && (nextStation.isSkip() || skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
						atoCmd.setNextStopPlatformId(nextStation.getPlatformId());
						//atoCmd.setSectionRunAdjustCmd((short) runtime);
						//stoptime = (nextStation.getPlanLeaveTime() - nextStation.getPlanArriveTime())/1000;
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
		String skipStatusStr = traincontrolHystrixService.getSkipStationStatus(nextStation.getPlatformId());
		LOG.info("[StaionSkipStatus] platformId:{} skipStatus:{}", nextStation.getPlatformId(), skipStatusStr);
		if(nextStation.getPlatformId() != 6 && skipStatusStr != null && skipStatusStr.equals("1") //人工设置跳停
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
			String nextPlatformIdStr = traincontrolHystrixService.getNextPlatformId(trainDir, platformId);
			//DstCodeEnum dstCodeEnum = DstCodeEnum.getByDstCode(dstCode);
			if(nextPlatformIdStr != null && nextPlatformIdStr.equals("error")){
				return null;
			}
			if(nextPlatformIdStr != null && !"error".equals(nextPlatformIdStr)){
				Integer nextPlatformId = Integer.parseInt(nextPlatformIdStr);

				String skipStatusStr1 = traincontrolHystrixService.getSkipStationStatus(nextPlatformId);
				System.out.println("platformId:"+nextPlatformId+" skipStatus:"+skipStatusStr1);
				//runtime += (int) ((nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime())/1000); // 区间运行时间（单位：秒）
				if(!(skipStatusStr1 != null && skipStatusStr1.equals("1"))) {//当前站台不是终点站，下一站没有跳停，则为该下一停车站台ID,否则为无效值
					atoCmd.setNextStopPlatformId(nextPlatformId);
					Integer stoptime = traincontrolHystrixService.getDefDwellTime(nextPlatformId);
					if(stoptime == null){
						return null;
					}
					cmd.setPlatformStopTime(stoptime);
					//atoCmd.setSectionRunAdjustCmd((short) runtime);
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
				//TrainRunTimetable nextStation = timetableList.get(i+1);
				thisStation.setStopTime((thisStation.getPlanLeaveTime() - thisStation.getPlanArriveTime())/1000);//当前站停站时间
				//thisStation.setRunTime((nextStation.getPlanArriveTime() - thisStation.getPlanLeaveTime())/1000);//当前站区间运行时间
				thisStation.setRunTime((thisStation.getPlanArriveTime() - prevStation.getPlanLeaveTime())/1000);
				runtask.getTrainRunTimetable().set(i, thisStation);
			}
			TrainRunTimetable lastStation = timetableList.get(timetableList.size() - 1);
			int stoptime = 30;//默认停站时间
			String resultMsg = trainrungraphHystrixService.getNextRuntask(runtask.getTraingroupnum(), runtask.getTablenum(), 
					runtask.getTrainnum(), lastStation.getPlatformId());
			if(resultMsg != null && !resultMsg.equals("error")){
				try{
					ObjectMapper objMapper = new ObjectMapper();
					objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
					TrainRunTask newtask = objMapper.readValue(resultMsg, TrainRunTask.class); // json转换成map
					TrainRunTimetable firstStation = newtask.getTrainRunTimetable().get(0);
					stoptime = (int) ((firstStation.getPlanLeaveTime() - lastStation.getPlanArriveTime())/1000);
				}catch (Exception e) {
					LOG.error("[calculateTime] runtask parse error!");
					e.printStackTrace();
				}
			}
			else{
				LOG.error("获取下一运行任务失败，不存在");
			}
			lastStation.setStopTime(stoptime);//当前站停站时间
			runtask.getTrainRunTimetable().set(timetableList.size()-1, lastStation);
		}
		return runtask;
	}
}
