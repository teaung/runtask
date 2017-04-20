package com.byd5.ats.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 应用层通用消息包
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 *     《LRTSW-SYS-ATS与CI通信接口协议》
 *     《LRTSW-SYS-ATS与ZC通信接口协议》
 * 
// cu_pub.h
// 通用信息结构体
typedef struct _header_info
{
	uint16_t inface_type;           // 接口类型
	uint32_t send_vender;           // 发送方厂商ID
	uint32_t receive_vender;        // 接收方厂商ID
	uint8_t  map_version;           // 电子地图版本
	uint32_t map_crc;               // 电子地图CRC校验
	uint32_t msg_cnum;              // 本消息周期计数
	uint8_t  comm_cycle;            // 通信周期
	uint32_t msg_snum_side;         // 对方消息序列号
	uint32_t msg_cnum_previous_msg; // 收到对方上一消息时，本方周期计数
	uint8_t  protocol_version;      // 协议版本
}header_info_t;

 */

public class AppProtocolHeader {
	
	/*
	 * 接口信息类型（2字节）：
	 * 0x9005：ATS-VOBC接口
	 * 0x9009：CI-ATS接口
	 * 0x9004：ZC-ATS接口
	 */
	@JsonProperty("inface_type")
	public int interfaceType;
	
	/*
	 * 源ID（4字节）：发送方标识
	 * 1）类型（1字节）：ATS=0x03；VOBC=0x01；CI=0x04；ZC=0x02
	 * 2）厂商ID（1字节）：对厂商的统一编号
	 * 3）设备ID（2字节）：ATS控制区ID/车载编号/CI控制区ID/ZC控制区ID
	 *   VOBC车载编号：15-14bit预留；13-2bit为列车编号；1-0bit为车端号
	 */
	@JsonProperty("send_vender")
	public int srcId;
	
	/*
	 * 目的ID（4字节）：接收方标识
	 * 1）类型（1字节）：ATS=0x03；VOBC=0x01；CI=0x04；ZC=0x02
	 * 2）厂商ID（1字节）：对厂商的统一编号
	 * 3）设备ID（2字节）：ATS控制区ID/车载编号/CI控制区ID/ZC控制区ID
	 *   VOBC车载编号：15-14bit预留；13-2bit为列车编号；1-0bit为车端号
	 */
	@JsonProperty("receive_vender")
	public int dstId;
	
	/*
	 * 电子地图版本（1字节）：
	 * 不使用时填写默认值0xff
	 */
	@JsonProperty("map_version")
	public short emapVersion;
	
	/*
	 * 电子地图CRC校验码（4字节）：
	 * 基于统一的电子地图的32位CRC校验码，其生成多项式为0x04C11DB7，
	 *   (x32 + x26 + x23 + x22 + x16 + x12 + x11 + x10 + x8 + x7 + x5 + x4 + x2 + x1 + 1)
	 * CRC寄存器初始值为0xffffffff
	 */
	@JsonProperty("map_crc")
	public int emapCrc;
	
	/*
	 * 消息序列号（4字节）：本消息本方的周期计数，发送方，每周期将本计数加1
	 */
	@JsonProperty("msg_cnum")
	public int sn;
	
	/*
	 * 通信周期（1字节）：单位50ms
	 */
	@JsonProperty("comm_cycle")
	public byte commCycle;
	
	/*
	 * 上一消息对方消息序列号（4字节）：记录收到对方上一条消息中的对方消息序列号
	 */
	@JsonProperty("msg_snum_side")
	public int sendsnAtLastRecvMessage;
	/*
	 * 上一消息本方消息序列号（4字节）：记录收到对方上一条消息时本方的周期计数
	 */
	@JsonProperty("msg_cnum_previous_msg")
	public int selfsnAtLastRecvMessage;
	
	/*
	 * 协议版本号（1字节）：VOBC-ATS的协议版本/CI-ATS协议版本/ZC-ATS协议版本；值范围1~255
	 */
	@JsonProperty("protocol_version")
	public byte protocolVersion;
	
/*	public short getInface_type() {
		return inface_type;
	}
	public void setInface_type(short inface_type) {
		this.inface_type = inface_type;
	}

	public int getSend_vender() {
		return send_vender;
	}
	public void setSend_vender(int send_vender) {
		this.send_vender = send_vender;
	}
	public int getReceive_vender() {
		return receive_vender;
	}
	public void setReceive_vender(int receive_vender) {
		this.receive_vender = receive_vender;
	}
	public byte getMap_version() {
		return map_version;
	}
	public void setMap_version(byte map_version) {
		this.map_version = map_version;
	}
	public int getMap_crc() {
		return map_crc;
	}
	public void setMap_crc(int map_crc) {
		this.map_crc = map_crc;
	}
	public int getMsg_cnum() {
		return msg_cnum;
	}
	public void setMsg_cnum(int msg_cnum) {
		this.msg_cnum = msg_cnum;
	}
	public byte getComm_cycle() {
		return comm_cycle;
	}
	public void setComm_cycle(byte comm_cycle) {
		this.comm_cycle = comm_cycle;
	}
	public int getMsg_snum_side() {
		return msg_snum_side;
	}
	public void setMsg_snum_side(int msg_snum_side) {
		this.msg_snum_side = msg_snum_side;
	}
	public int getMsg_cnum_previous_msg() {
		return msg_cnum_previous_msg;
	}
	public void setMsg_cnum_previous_msg(int msg_cnum_previous_msg) {
		this.msg_cnum_previous_msg = msg_cnum_previous_msg;
	}
	public byte getProtocol_version() {
		return protocol_version;
	}
	public void setProtocol_version(byte protocol_version) {
		this.protocol_version = protocol_version;
	}*/
	
}
