package com.byd5.ats.protocol.ats_vobc;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：车载设备日检状态信息帧（VOBC->ATS）
 * CU接收到VOBC的车载设备日检状态信息帧后，添加时间戳t_stamp，再发送给ATS
 * 
// cu_pub.h
// d. 总车载设备日检状态信息帧
typedef struct _amqp_vobc_every_check
{
    header_info_t header_info;
    msg_header_t msg_header;
    vobc2ats_train_check_t vobc2ats_train_t_every_check;
    cu2ats_time_t t_stamp;
}amqp_vobc_every_check_t;

 */
public class FrameTrainDailyCheck {
	
	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("vobc2ats_train_t_every_check")
	private AppDataTrainDailyCheck trainDailyCheck;
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataTrainDailyCheck getTrainDailyCheck() { return trainDailyCheck; }
	public void setTrainDailyCheck(AppDataTrainDailyCheck trainDailyCheck) { this.trainDailyCheck = trainDailyCheck; }

	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
}
