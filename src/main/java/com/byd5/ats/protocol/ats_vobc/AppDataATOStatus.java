package com.byd5.ats.protocol.ats_vobc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * ATO状态信息帧（VOBC->ATS）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 250ms周期发送
 * 通信超时中断时间为8s
 */
/*
// cu_pub.h
// a. ATO状态信息帧
typedef struct _vobc2ats_ato_status
{
	uint16_t  service_num;              // 服务号/表号
	uint16_t  line_num;                 // 线路编号
	uint16_t  train_line_num;           // 车组所属线路编号
	uint16_t  train_num;                // 车组号
	uint16_t  origin_line_num;          // 列车始发站线路编号 默认值 0xFFFF
	uint16_t  train_order_num;          // 车次号
	uint16_t  destin_line_num;          // 目的地线路编号 默认值 0xFFFF
	uint32_t  destin_num;               // 目的地号 默认值 0xFFFFFFFF
	uint16_t  driver_num;               // 司机号
	uint8_t   ato_mode;                 // ATO工作模式 AM自动驾驶：0x03 ATO未建立：0x00
	uint16_t  running_adjust_command;   // 区间运行调整命令
	uint8_t   cross_station_status;     // 跳停状态  跳停：0x55 无/取消跳停：0xAA
	uint8_t   detain_status;            // 扣车状态 扣车有效：0x55 扣车取消/无扣车：0xAA 无效值：0xFF
	uint16_t  next_station_id;          // 下一停车站台ID 默认值：0xFFFF
	uint32_t  reserv;                   // 预留 ？？？
}vobc2ats_ato_status_t;

 */
public class AppDataATOStatus {

	/*
	 * 消息类型（2字节）：0x0202=ATO状态信息（VOBC->ATS）
	 */
	//private short type;
	
	/*
	 * 服务号/表号（2字节）：00~65534
	 */
	@JsonProperty("service_num")
	public short serviceNum;
	
	/*
	 * 线路编号（2字节）：全网统一标识
	 */
	@JsonProperty("line_num")
	public short lineNum;
	
	/*
	 * 车组所属线路编号（2字节）：全网统一标识
	 */
	@JsonProperty("train_line_num")
	public short carLineNum;
	
	/*
	 * 车组号（2字节）：001~999；“车组所属线路号”+“车组号”在全网内为唯一标识
	 */
	@JsonProperty("train_num")
	public short carNum;
	
	/*
	 * 源线路编号（2字节）：列车始发站线路编号；默认值为0xffff；全网统一标识
	 */
	@JsonProperty("origin_line_num")
	public short srcLineNum;
	
	/*
	 * 车次号（2字节）：000～999有效；000表示车次号未设定
	 */
	@JsonProperty("train_order_num")
	public short trainNum;
	
	/*
	 * 目的地线路编号（2字节）：同线路编号；列车为非计划车时，发送默认值0xffff
	 */
	@JsonProperty("destin_line_num")
	public short dstLineNum;
	
	/*
	 * 目的地号（4字节）：用ASCII码标识，最多4个ASCII码，低于4个时高位用空格补齐；
	 * 列车为非计划车时，发送默认值0xffffffff
	 */
	@JsonProperty("destin_num")
	public int dstStationNum;
	
	/*
	 * 司机号（2字节）：001～999 
	 */
	@JsonProperty("driver_num")
	public short driverNum;
	
	/*
	 * ATO工作模式（1字节）：AM自动驾驶=0x03；ATO未建立=0x00； 
	 */
	@JsonProperty("ato_mode")
	public byte atoMode;
	
	/*
	 * 区间运行调整命令（2字节）：区间等级或区间运行时间；根据业主需求确定
	 */
	@JsonProperty("running_adjust_command")
	public int sectionRunLevel;
	
	/*
	 * 跳停状态（1字节）：跳停=0x55；无/取消跳停=0xAA；
	 */
	@JsonProperty("cross_station_status")
	public short skipStationStatus;

	/*
	 * 扣车状态（1字节）：扣车有效=0x55；扣车取消/无扣车=0xAA；
	 */
	@JsonProperty("detain_status")
	public short detainStationStatus;
	
	/*
	 * 下一停车站台ID（2字节）：定义同“跳停站台ID”，默认值为0xFFFF
	 */
	@JsonProperty("next_station_id")
	public int nextStationId;
	
	/*
	 * 预留（4字节）：
	 */
	@JsonProperty("reserv")
	public int reserved;
	
/*	public short getService_num() {
		return service_num;
	}
	public void setService_num(short service_num) {
		this.service_num = service_num;
	}
	public short getLine_num() {
		return line_num;
	}
	public void setLine_num(short line_num) {
		this.line_num = line_num;
	}
	
	public short getTrain_line_num() {
		return train_line_num;
	}
	public void setTrain_line_num(short train_line_num) {
		this.train_line_num = train_line_num;
	}
	public short getTrain_num() {
		return train_num;
	}
	public void setTrain_num(short train_num) {
		this.train_num = train_num;
	}
	public short getOrigin_line_num() {
		return origin_line_num;
	}
	public void setOrigin_line_num(short origin_line_num) {
		this.origin_line_num = origin_line_num;
	}
	public short getTrain_order_num() {
		return train_order_num;
	}
	public void setTrain_order_num(short train_order_num) {
		this.train_order_num = train_order_num;
	}
	public short getDestin_line_num() {
		return destin_line_num;
	}
	public void setDestin_line_num(short destin_line_num) {
		this.destin_line_num = destin_line_num;
	}
	public int getDestin_num() {
		return destin_num;
	}
	public void setDestin_num(int destin_num) {
		this.destin_num = destin_num;
	}
	
	public short getDriver_num() {
		return driver_num;
	}
	public void setDriver_num(short driver_num) {
		this.driver_num = driver_num;
	}
	public byte getAto_mode() {
		return ato_mode;
	}
	public void setAto_mode(byte ato_mode) {
		this.ato_mode = ato_mode;
	}
	
	public short getRunning_adjust_command() {
		return running_adjust_command;
	}
	public void setRunning_adjust_command(short running_adjust_command) {
		this.running_adjust_command = running_adjust_command;
	}

	public byte getCross_station_status() {
		return cross_station_status;
	}
	public void setCross_station_status(byte cross_station_status) {
		this.cross_station_status = cross_station_status;
	}

	public byte getDetain_status() {
		return detain_status;
	}
	public void setDetain_status(byte detain_status) {
		this.detain_status = detain_status;
	}
	public short getNext_station_id() {
		return next_station_id;
	}
	public void setNext_station_id(short next_station_id) {
		this.next_station_id = next_station_id;
	}
	public int getReserv() {
		return reserv;
	}
	public void setReserv(int reserv) {
		this.reserv = reserv;
	}*/
	
}
