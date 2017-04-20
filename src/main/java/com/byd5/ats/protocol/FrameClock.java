package com.byd5.ats.protocol;

import java.util.ArrayList;
import java.util.List;

import com.byd5.ats.protocol.ats_zc.AppDataZCTSRVerify;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：ATS心跳帧（ATS->VOBC/ZC/CI）
 * 由CU添加安全协议帧头后发送给VOBC/ZC/CI
 * 
// cu_pub.h
// a. 时钟信息帧
typedef struct _ats2vobc_clock
{
    header_info_t header_info;
    msg_header_t msg_header;
    ats_msg_time_t ats_msg_clock;
}ats2vobc_clock_t;
 */
public class FrameClock {
	
	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("ats_msg_clock")
	private AppDataClock clock;

	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataClock getClock() { return clock; }
	public void setClock(AppDataClock clock) { this.clock = clock; }
	
}
