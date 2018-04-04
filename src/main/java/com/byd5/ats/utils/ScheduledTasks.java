package com.byd5.ats.utils;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.byd.ats.protocol.ats_vobc.AppDataAVAtoCommand;
import com.byd5.ats.message.TrainEventPosition;
import com.byd5.ats.rabbitmq.ReceiverTrace;
import com.byd5.ats.rabbitmq.SenderDepart;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 定时器测试接口
 */
@Component
@Configurable
@EnableScheduling
public class ScheduledTasks {

	private TrainEventPosition event = new TrainEventPosition();
	
	private ObjectMapper mapper = new ObjectMapper(); // 转换器
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

	@Autowired
	ReceiverTrace ReceiverTrace;
	@Autowired
	SenderDepart send;
	
    //@Scheduled(fixedRate = 500 * 1)
    public void test() throws JsonParseException, JsonMappingException, IOException{
    	int len = 4;
		char[] code = {' ', ' ', ' ', ' '};
		char[] dst = "ZF".toCharArray();
		if (dst.length < len) {
			len = dst.length;
		}
		for (int i = 0; i < len; i ++) {
			code[3-i] = dst[len-1-i];
		}
		
    	AppDataAVAtoCommand cmd = new AppDataAVAtoCommand();
    	cmd.setType((short) 0x0203);
		cmd.setLength((short) 50);
    	cmd.setServiceNum(1);
    	cmd.setLineNum(64);
    	cmd.setCargroupLineNum(64);
    	cmd.setCargroupNum(102);
    	cmd.setSrcLineNum(64);
    	cmd.setTrainNum(102);
    	cmd.setDstLineNum(64);
    	cmd.setDstCode(code);
    	cmd.setPlanDir((short) 85);
    	cmd.setSkipPlatformId(65535);
    	cmd.setNextStopPlatformId(3);
    	cmd.setPlatformStopTime(30);
    	cmd.setNextSkipCmd((short) 170);
    	cmd.setSectionRunAdjustCmd(300);
    	cmd.setDetainCmd((short) 170);
    	cmd.setDoorctrlStrategy((short) 255);
    	cmd.setReserved(0x1010068);
    	
    	send.sendATOCommand(cmd);
    	
    	/*event.setServiceNum((short) 1);
		event.setTrainNum(102);
		event.setCargroupNum(101);
		event.setTimestamp(1505520000000L);
		event.setStation(2);
		event.setNextStationId(3);
		event.setSrc(101);
		
		String json = mapper.writeValueAsString(event);
		ReceiverTrace.receiveTraceStationEnter(json);*/
		logger.info("处理完成：");
    }
}
