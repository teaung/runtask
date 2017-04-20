package com.byd5.ats.protocol.ats_zc;

import java.util.ArrayList;
import java.util.List;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：TSR状态信息帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 250ms周期发送
 * ZC向ATS周期报告全部逻辑区段的限速执行状态。该信息作为TSR安全连接ZC->ATS方向的心跳信息。
 * 
 * CU接收到CI的站场状态信息帧后，添加时间戳t_stamp，再发送给ATS-serv
 * 
 * 
// cu_pub.h
// c. 总TSR状态信息帧
typedef struct _amqp_zc_status_tsr
{
    header_info_t zc_header_status;
    msg_header_t zc_msg_header_sta;
    zc2ats_status_tsr_t zc2ats_sta_tsr;
    uint16_t lgc_tsr_sta[LOGIC_TRACK_NUM];
    cu2ats_time_t t_stamp;
}amqp_zc_status_tsr_t;
 */
public class FrameZCTSRStatus {
	
	@JsonProperty("zc_header_status")
	private AppProtocolHeader frameHeader;
	@JsonProperty("zc_msg_header_sta")
	private AppDataHeader msgHeader;
	
	@JsonProperty("zc2ats_sta_tsr")
	private AppDataZCTSRStatus tsrStatus;
	
	@JsonProperty("lgc_tsr_sta")
	private List<Short> logicTSRValue = new ArrayList<Short>();
	
	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataZCTSRStatus getTsrStatus() { return tsrStatus; }
	public void setTsrStatus(AppDataZCTSRStatus tsrStatus) { this.tsrStatus = tsrStatus; }
	
	public List<Short> getLogicTSRValue() { return logicTSRValue; }
	public void setLogicTSRValue(List<Short> logicTSRValue) { this.logicTSRValue = logicTSRValue; } 
	
	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
	
}
