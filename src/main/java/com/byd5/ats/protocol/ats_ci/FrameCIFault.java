package com.byd5.ats.protocol.ats_ci;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：故障状态信息帧（CI->ATS）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 400ms周期发送
 * 通信超时中断时间为6s
 * 
 * CU接收到CI的故障状态信息帧后，添加时间戳t_stamp，再发送给ATS
 * 
 * 
// cu_pub.h
// b. amqp server ci msg error信息
typedef struct _amqp_ci_error_status
{
    header_info_t header_info;
    msg_header_t msg_header;
    ci_msg_error_t ci_msg_error;
    cu2ats_time_t t_stamp;

}amqp_ci_error_t;
 */
public class FrameCIFault {

	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("ci_msg_error")
	private AppDataCIFault ciFault;
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	
	public AppProtocolHeader getFrameHeader() {
		return frameHeader;
	}
	public void setFrameHeader(AppProtocolHeader frameHeader) {
		this.frameHeader = frameHeader;
	}
	public AppDataHeader getMsgHeader() {
		return msgHeader;
	}
	public void setMsgHeader(AppDataHeader msgHeader) {
		this.msgHeader = msgHeader;
	}

	public AppDataCIFault getCiFault() {
		return ciFault;
	}
	public void setCiFault(AppDataCIFault ciFault) {
		this.ciFault = ciFault;
	}
	public AppDataTimestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(AppDataTimestamp timestamp) {
		this.timestamp = timestamp;
	}
}
