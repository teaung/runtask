package com.byd5.ats.protocol.ats_ci;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppDataTimestamp;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：按钮及控制命令信息帧（ATS->CI）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 非周期发送
 * 通信超时中断时间为6s
 * 
// cu_pub.h
// b.  按钮及控制命令信息
typedef struct _ats2cu_ci_comm
{
    header_info_t header_info;
    msg_header_t msg_header;
    ats_msg_command_t ats_msg_command;
}ats2cu_ci_comm_t;
 */
public class FrameCICommand {

	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("ats_msg_command")
	private AppDataCICommand ciCommand;

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
	public AppDataCICommand getCiCommand() {
		return ciCommand;
	}
	public void setCiCommand(AppDataCICommand ciCommand) {
		this.ciCommand = ciCommand;
	}

}
