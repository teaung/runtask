/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byd5.ats.rabbitmq;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.message.TrainRunTask;
import com.byd5.ats.message.TrainRunTimetable;
import com.byd5.ats.service.RunTaskService;
import com.byd5.ats.utils.MyExceptionUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 接收运行调整的消息
 * 
 */
public class ReceiverAdjust {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverAdjust.class);
	
	@Autowired
	private RunTaskService runTaskHandler;
	
	@Autowired
	private SenderDepart sender;

	@RabbitListener(queues = "#{queueAdjust.name}")
	public void receiveAdjust(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[adjust] '" + in + "'");
		
		TrainRunTask adjustTask = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try{
			adjustTask = objMapper.readValue(in, TrainRunTask.class);
			
			// 更新运行任务列表
			Integer carNum = adjustTask.getTraingroupnum();
			runTaskHandler.updateMapRuntask(carNum, adjustTask);
			
			// 检查该车是否有记录
			/*TrainEventPosition event = runTaskHandler.getMapTrace(carNum);
			
			// 重新向该车发送下一站区间运行时间
			AppDataAVAtoCommand appDataATOCommand = null;
			if(event != null){
				//获取或 更新运行图任务信息
				TrainRunTask task = runTaskHandler.getMapRuntask(event);
				
				if(task != null){
					appDataATOCommand = runTaskHandler.aodCmdStationEnter(task, event);
					
					if(appDataATOCommand != null){
						//----------------------计划离站时间有改动----------------
						int platformId = event.getStation();
						TrainRunTimetable currStation = null;
						TrainRunTimetable nextStation = null;
						List<TrainRunTimetable> timetableList = adjustTask.getTrainRunTimetable();
						for (int i = 1; i < timetableList.size()-1; i ++) {//时刻表第一天跟最一条数据为折返轨数据，应忽略，只关注车站数据
							TrainRunTimetable t = timetableList.get(i);
							if (t.getPlatformId() == platformId) {
								currStation = t;
								nextStation = timetableList.get(i+1);
								break;
							}
						}
						int runtime = (int) (nextStation.getPlanArriveTime() - currStation.getPlanLeaveTime());
						if(runtime < 0){
							appDataATOCommand.setSectionRunAdjustCmd(runtime); //计划站停时间（单位：秒）0xFFFF
						}
						//------------------------------------------------------
						
						sender.sendATOCommand(appDataATOCommand);
					}
					
					
					LOG.info("[adjust] ATOCommand: next station ["
							+ appDataATOCommand.getNextStopPlatformId() + "] next section run time ["
							+ appDataATOCommand.getSectionRunAdjustCmd()+ "s]"
							+ "This station stop time ["+ appDataATOCommand.getPlatformStopTime()
							+ "s]");
				}
				
			}
			else {
				//报警？？？
				LOG.info("[adjust] not find the car (" + carNum + ") in trace list, so do nothing.");
			}*/
		}catch (Exception e) {
			// TODO: handle exception
			LOG.error("[adjust] parse data error!");
			MyExceptionUtil.printTrace2logger(e);
		}
		
		
		watch.stop();
		System.out.println("[adjust] Done in " + watch.getTotalTimeSeconds() + "s");
	}
	
	/*@RabbitListener(queues = "#{queueAdjust.name}")
	public void receiveAdjust(String in) throws JsonParseException, JsonMappingException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		LOG.info("[adjust] '" + in + "'");
		
		TrainRunTask adjustTask = null;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		adjustTask = objMapper.readValue(in, TrainRunTask.class);
		
		// 更新运行任务列表
		Integer carNum = adjustTask.getTraingroupnum();
		runTaskHandler.updateMapRuntask(carNum, adjustTask);
		
		// 检查该车是否有记录
		TrainEventPosition event = runTaskHandler.getMapTrace(carNum);
		
		// 重新向该车发送下一站区间运行时间
		AppDataAVAtoCommand appDataATOCommand = null;
		if(event != null){
			//获取或 更新运行图任务信息
			//TrainRunTask task = runTaskHandler.getMapRuntask(event);
			
			if(adjustTask != null){
				appDataATOCommand = runTaskHandler.aodCmdArriveAdjust(adjustTask, event);
				
				if(appDataATOCommand != null && appDataATOCommand.getPlatformStopTime()>=0 && appDataATOCommand.getSectionRunAdjustCmd()>0){
					sender.sendATOCommand(appDataATOCommand);
				}
			}
			
		}
		else {
			//报警？？？
			LOG.info("[adjust] not find the car (" + carNum + ") in trace list, so do nothing.");
		}
		
		watch.stop();
		System.out.println("[adjust] Done in " + watch.getTotalTimeSeconds() + "s");
	}*/
}
