package com.byd5.ats.protocol.ats_vobc;

import com.byd5.ats.protocol.AppDataHeader;
import com.byd5.ats.protocol.AppProtocolHeader;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用帧：ATO命令帧（ATS->VOBC）
 * 由CU添加安全协议帧头后发送给VOBC
 * 
// cu_pub.h
// b. ATO命令信息帧
typedef struct _ats2vobc_msg_comm
{
    header_info_t header_info;
    msg_header_t msg_header;
    ats2vobc_ato_command_t ats2vobc_ato_command;
}ats2vobc_msg_comm_t;
 */
public class FrameATOCommand {

	@JsonProperty("header_info")
	private AppProtocolHeader frameHeader;
	@JsonProperty("msg_header")
	private AppDataHeader msgHeader;
	
	@JsonProperty("ats2vobc_ato_command")
	private AppDataATOCommand atoCommand;
		
	public AppProtocolHeader getFrameHeader() { return frameHeader; }
	public void setFrameHeader(AppProtocolHeader frameHeader) { this.frameHeader = frameHeader; }
	public AppDataHeader getMsgHeader() { return msgHeader; }
	public void setMsgHeader(AppDataHeader msgHeader) { this.msgHeader = msgHeader; }

	public AppDataATOCommand getAtoCommand() { return atoCommand; }
	public void setAtoCommand(AppDataATOCommand atoCommand) { this.atoCommand = atoCommand; }

}
