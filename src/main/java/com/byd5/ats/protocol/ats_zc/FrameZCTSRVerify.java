package com.byd5.ats.protocol.ats_zc;

import java.util.ArrayList;
import java.util.List;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：TSR验证命令帧（ATS->ZC）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ATS向ZC发送TSR验证命令信息包，要求ZC对该TSR命令进行可执行性校验。
 * 该命令信息ATS应向ZC持续下达3秒钟时间，直到收到ZC回复的命令应答信息或超时退出。TSR信息为双向有效
 * 
 * 
// cu_pub.h
// b. TSR验证命令帧
typedef struct _ats2cu_zc_t_verify
{
    header_info_t header_info_ver;
    msg_header_t msg_header_ver;
    ats2zc_verify_tsr_t verify_tsr;
    ats2zc_logic_t_id_t lg_t_id[LOGIC_TRACK_NUM];
}ats2cu_zc_t_verify_t;
// b. TSR验证命令帧
typedef struct _ats2cu_verify
{
    header_info_t header_info_ver;
    msg_header_t msg_header_ver;
    ats2zc_verify_tsr_t verify_tsr;
    uint16_t lg_id[LOGIC_TRACK_NUM];
}ats2cu_verify_t;
 */
public class FrameZCTSRVerify {

	@JsonProperty("header_info_ver")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header_ver")
	private AppDataHeader msgHeader;
	
	@JsonProperty("verify_tsr")
	private AppDataZCTSRVerify tsrVerify;
	
	@JsonProperty("lg_id")
	private List<Short> logicId = new ArrayList<Short>();

	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataZCTSRVerify getTsrVerify() { return tsrVerify; }
	public void setTsrVerify(AppDataZCTSRVerify tsrVerify) { this.tsrVerify = tsrVerify; }
	
	public List<Short> getLogicId() { return logicId; }
	public void setLogicId(List<Short> logicId) { this.logicId = logicId; } 
	
}
