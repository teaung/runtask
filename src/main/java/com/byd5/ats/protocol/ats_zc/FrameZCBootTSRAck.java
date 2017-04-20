package com.byd5.ats.protocol.ats_zc;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：TSR验证命令确认帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * 
 * 
// cu_pub.h
// d. 上电TSR确认
typedef struct _ats2cu_zc_t_elec
{
    header_info_t header_info_elec;
    msg_header_t msg_header_elec;
    ats2zc_electrify_tsr_t elec_tsr;
}ats2cu_zc_t_elec_t;
 */
public class FrameZCBootTSRAck {

	@JsonProperty("header_info_elec")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header_elec")
	private AppDataHeader msgHeader;
	
	@JsonProperty("elec_tsr")
	private AppDataZCBootTSRAck bootTSRAck;

	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataZCBootTSRAck getTsrExcute() { return bootTSRAck; }
	public void setTsrExcute(AppDataZCBootTSRAck bootTSRAck) { this.bootTSRAck = bootTSRAck; }
	
}
