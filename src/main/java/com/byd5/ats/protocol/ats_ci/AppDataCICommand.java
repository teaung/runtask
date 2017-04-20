package com.byd5.ats.protocol.ats_ci;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 按钮及控制命令信息帧（ATS->CI）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 非周期发送
 * 通信超时中断时间为6s
 */
/*
// cu_pub.h
// a. 按钮及控制命令信息
typedef struct _ats_msg_command
{
	uint8_t  command_num;               // 控制命令个数
	uint8_t  command_type;              // 控制命令类型 
	uint32_t object_id;                 // 控制命令参数 -> 设备ID
	uint8_t  object_type;               // 控制命令参数 -> 设备类型
	uint8_t  object_other;              // 控制命令参数 -> 设备其他
}ats_msg_command_t;
 */
public class AppDataCICommand {

	/*
	 * 消息类型（2字节）：0x0203=按钮及控制命令信息（ATS->CI）
	 */
	//private short type;
	
	/*
	 * 控制命令个数（1字节）：ATS向CI发送的控制命令个数
	 */
	@JsonProperty("command_num")
	public byte  cmdNum;               // 控制命令个数
	
	/*
	 * 控制命令1类型（1字节）：发送的控制命令类型，具体见附录C；
	 * 1）办理进路：0x01 + 进路ID
	 * 2）设置保护区段：0x02 + 保护区段ID
	 * ......
	 */
	@JsonProperty("command_type")
	public byte  cmdType;              // 控制命令类型 
	
	/*
	 * 命令参数（6字节）：控制命令参数，具体见附录C；
	 * 命令参数前4个字节为设备ID + 第3个字节描述设备类型 + 第4个字节描述其他信息（默认为0xFF）。
	 */
	@JsonProperty("object_id")
	public int   deviceId;                 // 控制命令参数 -> 设备ID
	@JsonProperty("object_type")
	public byte  deviceType;               // 控制命令参数 -> 设备类型
	@JsonProperty("object_other")
	public byte  deviceOther;              // 控制命令参数 -> 设备其他
	
	/*
	 * 控制命令n类型（1字节）：发送的控制命令类型，具体见附录C；
	 * 1）办理进路：0x01 + 进路ID
	 * 2）设置保护区段：0x02 + 保护区段ID
	 * ......
	 */
	/*
	 * 命令参数（6字节）：控制命令参数，具体见附录C；
	 * 命令参数前4个字节为设备ID + 第3个字节描述设备类型 + 第4个字节描述其他信息（默认为0xFF）。
	 */
	
	
/*	public byte getCommand_num() {
		return command_num;
	}
	public void setCommand_num(byte command_num) {
		this.command_num = command_num;
	}

	public byte getCommand_type() {
		return command_type;
	}
	public void setCommand_type(byte command_type) {
		this.command_type = command_type;
	}
	
	public int getObject_id() {
		return object_id;
	}
	public void setObject_id(int object_id) {
		this.object_id = object_id;
	}
	public byte getObject_type() {
		return object_type;
	}
	public void setObject_type(byte object_type) {
		this.object_type = object_type;
	}
	public byte getObject_other() {
		return object_other;
	}
	public void setObject_other(byte object_other) {
		this.object_other = object_other;
	}*/
}
