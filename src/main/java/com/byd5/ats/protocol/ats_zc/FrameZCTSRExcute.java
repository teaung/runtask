package com.byd5.ats.protocol.ats_zc;

import java.util.ArrayList;
import java.util.List;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：TSR执行命令帧（ATS->ZC）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ATS向ZC发送TSR执行命令，用于要求ZC执行发布该限速信息。
 * 该命令信息ATS应向ZC持续下达3秒钟时间，直到收到ZC回复的命令应答信息或超时退出
 * 
 * 
// cu_pub.h
// c. TSR执行命令确认帧 
typedef struct _ats2cu_execute
{
    header_info_t header_info_exec;
    msg_header_t msg_header_exec;
    ats2zc_execute_tsr_t execue_tsr;
    uint16_t lg_id[LOGIC_TRACK_NUM];
}ats2cu_execute_t;
 */
public class FrameZCTSRExcute {

	@JsonProperty("header_info_exec")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header_exec")
	private AppDataHeader msgHeader;
	
	@JsonProperty("execue_tsr")
	private AppDataZCTSRExcute tsrExcute;
	
	@JsonProperty("lg_id")
	private List<Short> logicId = new ArrayList<Short>();

	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataZCTSRExcute getTsrExcute() { return tsrExcute; }
	public void setTsrExcute(AppDataZCTSRExcute tsrExcute) { this.tsrExcute = tsrExcute; }
	
	public List<Short> getLogicId() { return logicId; }
	public void setLogicId(List<Short> logicId) { this.logicId = logicId; } 
}
