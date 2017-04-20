package com.byd5.ats.protocol.ats_vobc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * ATO命令信息帧（ATS->VOBC）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 */
/*
// cu_pub.h
// a. ATO命令信息帧
typedef struct _ats2vobc_ato_command
{
	uint16_t  service_num;              // 服务号/表号
	uint16_t  line_num;                 // 线路编号
	uint32_t  next_zc_id;               // VOBC最大安全前端所在ZC管辖区域的下一个ZC ID 默认值为0xFFFFFFFF
	uint32_t  next_ci_id;               // VOBC最大安全前端所在ci管辖区域的下一个ci ID 默认值为0xFFFFFFFF
	uint32_t  next_ats_id;              // VOBC最大安全前端所在ats管辖区域的下一个ats ID 默认值为 0xFFFFFFFF
	uint16_t  train_line_num;           // 车组所属线路编号
	uint16_t  train_num;                // 车组号
	uint16_t  origin_line_num;          // 列车始发站线路编号 默认值 0xFFFF
	uint16_t  train_order_num;          // 车次号
	uint16_t  destin_line_num;          // 目的地线路编号 默认值 0xFFFF
	uint32_t  destin_num;               // 目的地号 默认值 0xFFFFFFFF
	uint8_t   direction_plan;           // 计划运行方向  上行 0x55 下行 0xAA 其他 0xFF
	uint16_t  cross_station_id;         // 跳停站台ID 下一站跳停：站台ID 下一站不跳停：0xFFFF
	uint16_t  next_station_id;          // 下一停车站台ID 默认值：0xFFFF
	uint16_t  stop_station_time;        // 站停时间 立即发车：0x0001 站停时间：大于0x0002，单位秒 无效值：0xFFFF
	uint8_t  cross_station_command;     // 下一站跳停命令 下一站跳停：0x55 下一站无/取消跳停：0xAA 无效值：0xFF
	uint16_t  running_adjust_command;   // 区间运行调整命令
	uint8_t   detain_command;           // 扣车命令 扣车有效：0x55 扣车取消/无扣车：0xAA 无效值：0xFF
	uint8_t   turnback_command;         // 折返命令 站前折返：0x55 有人站后折返：0xCC 无人自动折返：0xAA 不折返：0xFF
	uint8_t   turn_command;             // 回段指示 回段：0x55 不回段：0xAA 默认值：0xFF
	uint8_t   door_control;             // 门控策略 开左门：0x55 开右门：0xCC 同时开双侧门：0xAA 先开左门再开右门：0x11 先开右门再开左门：0x22 默认值：0xFF
	uint32_t  reserv;                   // 预留 ？？？
}ats2vobc_ato_command_t;
 */
public class AppDataATOCommand {

	/*
	 * 消息类型（2字节）：0x0203=ATO命令信息（ATS->VOBC）
	 */
	//private short type;
	
	/*
	 * 服务号/表号（2字节）：列车为非计划车时，发送默认值0xffff
	 */
	@JsonProperty("service_num")
	public short serviceNum;
	
	/*
	 * 线路编号（2字节）：全网统一标识
	 */
	@JsonProperty("line_num")
	public short lineNum;
	
	/*
	 * 下一ZC ID（4字节）：VOBC最大安全前端所在ZC管辖区域的下一个ZC ID；默认值为0xffffffff
	 */
	@JsonProperty("next_zc_id")
	public int nextZcId;
	
	/*
	 * 下一CI ID（4字节）：VOBC最大安全前端所在CI管辖区域的下一个CI ID；默认值为0xffffffff
	 */
	@JsonProperty("next_ci_id")
	public int nextCiId;
	
	/*
	 * 下一ATS ID（4字节）：VOBC最大安全前端所在ATS管辖区域的下一个ATS ID；默认值为0xffffffff
	 */
	@JsonProperty("next_ats_id")
	public int nextAtsId;

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
	 * 车次号（2字节）：0000～9999；默认值0000
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
	 * 计划运行方向（1字节）：上行=0x55；下行=0xAA 
	 */
	@JsonProperty("direction_plan")
	public short directionPlan;
	
	/*
	 * 跳停站台ID（2字节）：下一站跳停=站台ID；下一站不跳停=0xFFFF 
	 */
	@JsonProperty("cross_station_id")
	public int skipStationId;
	
	/*
	 * 下一停车站台ID（2字节）：定义同“跳停站台ID”，默认值为0xFFFF
	 */
	@JsonProperty("next_station_id")
	public int nextStationId;
	
	/*
	 * 站停时间（2字节）：立即发车=0x0001；站停时间=大于0x0002，单位秒；无效值=0xFFFF
	 */
	@JsonProperty("stop_station_time")
	public int stationStopTime;
	
	/*
	 * 下一站跳停命令（1字节）：下一站跳停=0x55；下一站无/取消跳停=0xAA；无效值=0xFF
	 */
	@JsonProperty("cross_station_command")
	public short skipNextStation;
	
	/*
	 * 区间运行调整命令（2字节）：区间等级或区间运行时间；根据业主需求确定
	 */
	@JsonProperty("running_adjust_command")
	public int sectionRunLevel;
	
	/*
	 * 扣车命令（1字节）：扣车有效=0x55；扣车取消/无扣车=0xAA；无效值=0xFF
	 */
	@JsonProperty("detain_command")
	public short detainCmd;
	
	/*
	 * 折返命令（1字节）：站前折返=0x55；有人站后折返=0xCC；无人站后折返=0xAA；不折返=0xFF
	 */
	@JsonProperty("turnback_command")
	public short returnCmd;
	
	/*
	 * 回段指示（1字节）：回段=0x55；不回段=0xAA；默认值0xFF
	 */
	@JsonProperty("turn_command")
	public short gotoRailYard;
	
	/*
	 * 门控策略（1字节）：开左门=0x55；开右门=0xCC；同时开双侧门=0xAA；先开左门再开右门=0x11；先开右门再开左门=0x22；默认值0xFF
	 * 开门间隔在具体工程项目中确认；站台为双侧门时，发送门控策略；站台为单侧门时，发送默认值。
	 */
	@JsonProperty("door_control")
	public short doorControl;
	
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
	public int getNext_zc_id() {
		return next_zc_id;
	}
	public void setNext_zc_id(int next_zc_id) {
		this.next_zc_id = next_zc_id;
	}
	public int getNext_ci_id() {
		return next_ci_id;
	}
	public void setNext_ci_id(int next_ci_id) {
		this.next_ci_id = next_ci_id;
	}
	public int getNext_ats_id() {
		return next_ats_id;
	}
	public void setNext_ats_id(int next_ats_id) {
		this.next_ats_id = next_ats_id;
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
	public byte getDirection_plan() {
		return direction_plan;
	}
	public void setDirection_plan(byte direction_plan) {
		this.direction_plan = direction_plan;
	}
	public short getCross_station_id() {
		return cross_station_id;
	}
	public void setCross_station_id(short cross_station_id) {
		this.cross_station_id = cross_station_id;
	}
	public short getNext_station_id() {
		return next_station_id;
	}
	public void setNext_station_id(short next_station_id) {
		this.next_station_id = next_station_id;
	}

	public short getStop_station_time() {
		return stop_station_time;
	}
	public void setStop_station_time(short stop_station_time) {
		this.stop_station_time = stop_station_time;
	}
	public byte getCross_station_command() {
		return cross_station_command;
	}
	public void setCross_station_command(byte cross_station_command) {
		this.cross_station_command = cross_station_command;
	}
	public short getRunning_adjust_command() {
		return running_adjust_command;
	}
	public void setRunning_adjust_command(short running_adjust_command) {
		this.running_adjust_command = running_adjust_command;
	}
	public byte getDetain_command() {
		return detain_command;
	}
	public void setDetain_command(byte detain_command) {
		this.detain_command = detain_command;
	}
	public byte getTurnback_command() {
		return turnback_command;
	}
	public void setTurnback_command(byte turnback_command) {
		this.turnback_command = turnback_command;
	}
	public byte getTurn_command() {
		return turn_command;
	}
	public void setTurn_command(byte turn_command) {
		this.turn_command = turn_command;
	}
	public byte getDoor_control() {
		return door_control;
	}
	public void setDoor_control(byte door_control) {
		this.door_control = door_control;
	}
	public int getReserv() {
		return reserv;
	}
	public void setReserv(int reserv) {
		this.reserv = reserv;
	}*/
	
}
