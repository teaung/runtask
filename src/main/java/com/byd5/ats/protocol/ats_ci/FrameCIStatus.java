package com.byd5.ats.protocol.ats_ci;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：站场状态信息帧（CI->ATS）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 400ms周期发送
 * 通信超时中断时间为6s
 * 
 * CU接收到CI的站场状态信息帧后，添加时间戳t_stamp，再发送给ATS
 * 
 * 
// cu_pub.h
// a. amqp server ci status信息
typedef struct _amqp_ci_status
{
    header_info_t header_info;
    msg_header_t msg_header;
    ci_msg_status_t ci_status;
    dev_id_t dev_id;
    cu2ats_time_t t_stamp;

}amqp_ci_status_t;
 */
public class FrameCIStatus {

	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("ci_status")
	private AppDataCIStatus ciStatus;
	
	@JsonProperty("dev_id")
	private CIDeviceId ciDeviceId;
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }

	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataCIStatus getCiStatus() { return ciStatus; }
	public void setCiStatus(AppDataCIStatus ciStatus) { this.ciStatus = ciStatus; }
	
	public CIDeviceId getCiDeviceId() { return ciDeviceId; }
	public void setCiDeviceId(CIDeviceId ciDeviceId) { this.ciDeviceId = ciDeviceId; } 
	
	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
}
