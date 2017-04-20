package com.byd5.ats.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用层数据格式头部
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 *     《LRTSW-SYS-ATS与CI通信接口协议》
 *     《LRTSW-SYS-ATS与ZC通信接口协议》
 * 
 * // cu_pub.h
 * typedef struct _msg_header
 * {
 *     uint16_t msg_len;               // 信息长度 
 *     uint16_t msg_type;              // 信息类型
 * }msg_header_t;
 */
public class AppDataHeader {

	/*
	 * 报文长度（2字节）：报文类型至报文结束的字节数
	 */
	@JsonProperty("msg_len")
	public short length;
	
	/*
	 * 报文类型（2字节）：定义某一条应用信息的标识
	 */
	@JsonProperty("msg_type")
	public short type;
	
/*	public short getMsg_len() {
		return msg_len;
	}
	public void setMsg_len(short msg_len) {
		this.msg_len = msg_len;
	}
	
	public short getMsg_type() {
		return msg_type;
	}
	public void setMsg_type(short msg_type) {
		this.msg_type = msg_type;
	}*/
	
}
