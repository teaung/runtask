package com.byd5.ats.protocol.ats_vobc;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：ATO状态帧（VOBC->ATS）
 * CU接收到VOBC的ATO状态帧后，添加时间戳t_stamp，再发送给ATS
 * 
// cu_pub.h
// b. 列车状态
typedef struct _amqp_vobc_train_status
{
    header_info_t header_info;
    msg_header_t msg_header;
    vobc2ats_train_msg_t train_status;
    cu2ats_time_t t_stamp;

}amqp_vobc_train_status_t;
 */
public class FrameTrainStatus {
	
	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("train_status")
	private AppDataTrainStatus trainStatus;
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataTrainStatus getTrainStatus() { return trainStatus; }
	public void setTrainStatus(AppDataTrainStatus trainStatus) { this.trainStatus = trainStatus; }

	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
	
}
