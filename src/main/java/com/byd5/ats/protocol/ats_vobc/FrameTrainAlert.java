package com.byd5.ats.protocol.ats_vobc;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：车载设备报警信息帧（VOBC->ATS）
 * CU接收到VOBC的车载设备报警信息帧后，添加时间戳t_stamp，再发送给ATS
 * 
// cu_pub.h
// c. 总车载设备报警信息帧
typedef struct _amqp_vobc_warn
{
    header_info_t header_info;
    msg_header_t msg_header;
    vobc2ats_train_warning_t vobc2ats_train_warn;
    cu2ats_time_t t_stamp;
}amqp_vobc_warn_t;

 */
public class FrameTrainAlert {
	
	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("vobc2ats_train_warn")
	private AppDataTrainAlert trainAlert;
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataTrainAlert getTrainAlert() { return trainAlert; }
	public void setTrainAlert(AppDataTrainAlert trainAlert) { this.trainAlert = trainAlert; }

	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
}
