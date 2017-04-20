package com.byd5.ats.protocol.ats_zc;

import java.util.ArrayList;
import java.util.List;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：TSR执行命令确认帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ZC向ATS发送TSR执行命令，用于通知ATS当前ZC对该TSR命令执行的结果；该命令信息ZC应向ATS持续反馈3秒钟时间。
 * 
 * CU接收到CI的站场状态信息帧后，添加时间戳t_stamp，再发送给ATS-serv
 * 
 * 
// cu_pub.h
// b. 总TSR执行命令确认帧
typedef struct _amqp_zc_execute_tsr
{
    header_info_t zc_header_execute;
    msg_header_t zc_msg_header_execute;
    zc2ats_execute_tsr_t zc2ats_execue_tsr;
    uint16_t lgc_id_execu[LOGIC_TRACK_NUM];
    cu2ats_time_t t_stamp;
}amqp_zc_execute_tsr_t;
 */
public class FrameZCTSRExcuteAck {

	@JsonProperty("zc_header_execute")
	private AppProtocolHeader frameHeader;
	@JsonProperty("zc_msg_header_execute")
	private AppDataHeader msgHeader;
	
	@JsonProperty("zc2ats_execue_tsr")
	private AppDataZCTSRExcuteAck tsrExcuteAck;
	
	@JsonProperty("lgc_id_execu")
	private List<Short> logicId = new ArrayList<Short>();

	@JsonProperty("t_stamp")
	private AppDataTimestamp timestamp;
	
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataZCTSRExcuteAck getTsrExcuteAck() { return tsrExcuteAck; }
	public void setTsrExcuteAck(AppDataZCTSRExcuteAck tsrExcuteAck) { this.tsrExcuteAck = tsrExcuteAck; }
	
	public List<Short> getLogicId() { return logicId; }
	public void setLogicId(List<Short> logicId) { this.logicId = logicId; } 
	
	public AppDataTimestamp getTimestamp() { return timestamp; }
	public void setTimestamp(AppDataTimestamp timestamp) { this.timestamp = timestamp; }
}
