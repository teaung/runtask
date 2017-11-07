/*package com.byd5.ats.message;

import com.fasterxml.jackson.annotation.JsonProperty;


 * ATO命令信息帧（ATS->VOBC）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 

// cu_pub.h
// a. ATO命令信息帧
typedef struct _ats2vobc_ato_command
{
	uint16_t  service_num;              // 服务号/表号 00~65534有效 默认值：0xFFFF | 列车为非计划车时，发送默认值 |
	uint16_t  line_num;                 // 线路编号 全网统一标识 |
	uint32_t  next_zc_id;               // （预留）VOBC最大安全前端所在ZC管辖区域的下一个ZC ID 默认值为0xFFFFFFFF
	uint32_t  next_ci_id;               // （预留）VOBC最大安全前端所在ci管辖区域的下一个ci ID 默认值为0xFFFFFFFF
	uint32_t  next_ats_id;              // （预留）VOBC最大安全前端所在ats管辖区域的下一个ats ID 默认值为 0xFFFFFFFF
	uint16_t  train_line_num;           // 车组所属线路编号| 车组所属线路编号 | 全网统一标识 |
	uint16_t  train_num;                // 车组号| 001~999 | “车组所属线路号”+“车组号”在全线网内为唯一标识 |
	uint16_t  origin_line_num;          // 列车始发站线路编号 默认值 0xFFFF
	uint16_t  train_order_num;          // 车次号| 0001~9999 | 默认值0000 |
	uint16_t  destin_line_num;          // 目的地线路编号| 目的地线路编号，同线路编号 默认值：0xFFFF | 列车为非计划车时，发送默认值 |
	uint32_t  destin_num;               // 目的地号 默认值 0xFFFFFFFF  列车为非计划车时，发送默认值
	uint8_t   direction_plan;           // 计划运行方向  上行 0x55 下行 0xAA 其他 0xFF
	uint16_t  cross_station_id;         // 跳停站台ID 下一站跳停：站台ID 下一站不跳停：0xFFFF
	uint16_t  next_station_id;          // 下一停车站台ID 默认值：0xFFFF
	uint16_t  stop_station_time;        // 站停时间 立即发车：0x0001 站停时间：大于0x0002，单位秒 无效值：0xFFFF
	uint8_t  cross_station_command;     // 下一站跳停命令 下一站跳停：0x55 下一站无/取消跳停：0xAA 无效值：0xFF
	uint16_t  running_adjust_command;   // （预留）区间运行调整命令
	uint8_t   detain_command;           // （预留）扣车命令 扣车有效：0x55 扣车取消/无扣车：0xAA 无效值：0xFF
	uint8_t   turnback_command;         // （预留）折返命令 站前折返：0x55 有人站后折返：0xCC 无人自动折返：0xAA 不折返：0xFF
	uint8_t   turn_command;             // （预留）回段指示 回段：0x55 不回段：0xAA 默认值：0xFF
	uint8_t   door_control;             // 门控策略 开左门：0x55 开右门：0xCC 同时开双侧门：0xAA 先开左门再开右门：0x11 先开右门再开左门：0x22 默认值：0xFF
	uint32_t  reserv;                   // 预留 ？？？
}ats2vobc_ato_command_t;
 
public class AppDataATOCommand {

	
	 * 消息类型（2字节）：0x0203=ATO命令信息（ATS->VOBC）
	 
	//private short type;
	
	
	 * 服务号/表号（2字节）：列车为非计划车时，发送默认值0xffff
	 
	@JsonProperty("service_num")
	private short serviceNum;
	
	
	 * 线路编号（2字节）：全网统一标识
	 
	@JsonProperty("line_num")
	private short lineNum;
	
	
	 * 下一ZC ID（4字节）：VOBC最大安全前端所在ZC管辖区域的下一个ZC ID；默认值为0xffffffff
	 
	@JsonProperty("next_zc_id")
	private int nextZcId;
	
	
	 * 下一CI ID（4字节）：VOBC最大安全前端所在CI管辖区域的下一个CI ID；默认值为0xffffffff
	 
	@JsonProperty("next_ci_id")
	private int nextCiId;
	
	
	 * 下一ATS ID（4字节）：VOBC最大安全前端所在ATS管辖区域的下一个ATS ID；默认值为0xffffffff
	 
	@JsonProperty("next_ats_id")
	private int nextAtsId;

	
	 * 车组所属线路编号（2字节）：全网统一标识
	 
	@JsonProperty("train_line_num")
	private short carLineNum;
	
	
	 * 车组号（2字节）：001~999；“车组所属线路号”+“车组号”在全网内为唯一标识
	 
	@JsonProperty("train_num")
	private short carNum;
	
	
	 * 源线路编号（2字节）：列车始发站线路编号；默认值为0xffff；全网统一标识
	 
	@JsonProperty("origin_line_num")
	private short srcLineNum;
	
	
	 * 车次号（2字节）：0000～9999；默认值0000
	 
	@JsonProperty("train_order_num")
	private short trainNum;
	
	
	 * 目的地线路编号（2字节）：同线路编号；列车为非计划车时，发送默认值0xffff
	 
	@JsonProperty("destin_line_num")
	private short dstLineNum;
	
	
	 * 目的地号（4字节）：用ASCII码标识，最多4个ASCII码，低于4个时高位用空格补齐；
	 * 列车为非计划车时，发送默认值0xffffffff
	 
	@JsonProperty("destin_num")
	private String dstStationNum;
	
	
	 * 计划运行方向（1字节）：上行=0x55；下行=0xAA 
	 
	@JsonProperty("direction_plan")
	private short directionPlan;
	
	
	 * 跳停站台ID（2字节）：下一站跳停=站台ID；下一站不跳停=0xFFFF 
	 
	@JsonProperty("cross_station_id")
	private int skipStationId;
	
	
	 * 下一停车站台ID（2字节）：定义同“跳停站台ID”，默认值为0xFFFF
	 
	@JsonProperty("next_station_id")
	private int nextStationId;
	
	
	 * 站停时间（2字节）：立即发车=0x0001；站停时间=大于0x0002，单位秒；无效值=0xFFFF
	 
	@JsonProperty("stop_station_time")
	private int stationStopTime;
	
	
	 * 下一站跳停命令（1字节）：下一站跳停=0x55；下一站无/取消跳停=0xAA；无效值=0xFF
	 
	@JsonProperty("cross_station_command")
	private short skipNextStation;
	
	
	 * 区间运行调整命令（2字节）：区间等级或区间运行时间；根据业主需求确定
	 
	@JsonProperty("running_adjust_command")
	private int sectionRunLevel;
	
	
	 * 扣车命令（1字节）：扣车有效=0x55；扣车取消/无扣车=0xAA；无效值=0xFF
	 
	@JsonProperty("detain_command")
	private short detainCmd;
	
	
	 * 折返命令（1字节）：站前折返=0x55；有人站后折返=0xCC；无人站后折返=0xAA；不折返=0xFF
	 
	@JsonProperty("turnback_command")
	private short returnCmd;
	
	
	 * 回段指示（1字节）：回段=0x55；不回段=0xAA；默认值0xFF
	 
	@JsonProperty("turn_command")
	private short gotoRailYard;
	
	
	 * 门控策略（1字节）：开左门=0x55；开右门=0xCC；同时开双侧门=0xAA；先开左门再开右门=0x11；先开右门再开左门=0x22；默认值0xFF
	 * 开门间隔在具体工程项目中确认；站台为双侧门时，发送门控策略；站台为单侧门时，发送默认值。
	 
	@JsonProperty("door_control")
	private short doorControl;
	
	
	 * 预留（4字节）：
	 
	@JsonProperty("reserv")
	private int reserved;

	
	public short getServiceNum() {
		return serviceNum;
	}

	public void setServiceNum(short serviceNum) {
		this.serviceNum = serviceNum;
	}

	public short getLineNum() {
		return lineNum;
	}

	public void setLineNum(short lineNum) {
		this.lineNum = lineNum;
	}

	public int getNextZcId() {
		return nextZcId;
	}

	public void setNextZcId(int nextZcId) {
		this.nextZcId = nextZcId;
	}

	public int getNextCiId() {
		return nextCiId;
	}

	public void setNextCiId(int nextCiId) {
		this.nextCiId = nextCiId;
	}

	public int getNextAtsId() {
		return nextAtsId;
	}

	public void setNextAtsId(int nextAtsId) {
		this.nextAtsId = nextAtsId;
	}

	public short getCarLineNum() {
		return carLineNum;
	}

	public void setCarLineNum(short carLineNum) {
		this.carLineNum = carLineNum;
	}

	public short getCarNum() {
		return carNum;
	}

	public void setCarNum(short carNum) {
		this.carNum = carNum;
	}

	public short getSrcLineNum() {
		return srcLineNum;
	}

	public void setSrcLineNum(short srcLineNum) {
		this.srcLineNum = srcLineNum;
	}

	public short getTrainNum() {
		return trainNum;
	}

	public void setTrainNum(short trainNum) {
		this.trainNum = trainNum;
	}

	public short getDstLineNum() {
		return dstLineNum;
	}

	public void setDstLineNum(short dstLineNum) {
		this.dstLineNum = dstLineNum;
	}

	public String getDstStationNum() {
		return dstStationNum;
	}

	public void setDstStationNum(String dstStationNum) {
		this.dstStationNum = dstStationNum;
	}

	public short getDirectionPlan() {
		return directionPlan;
	}

	public void setDirectionPlan(short directionPlan) {
		this.directionPlan = directionPlan;
	}

	public int getSkipStationId() {
		return skipStationId;
	}

	public void setSkipStationId(int skipStationId) {
		this.skipStationId = skipStationId;
	}

	public int getNextStationId() {
		return nextStationId;
	}

	public void setNextStationId(int nextStationId) {
		this.nextStationId = nextStationId;
	}

	public int getStationStopTime() {
		return stationStopTime;
	}

	public void setStationStopTime(int stationStopTime) {
		this.stationStopTime = stationStopTime;
	}

	public short getSkipNextStation() {
		return skipNextStation;
	}

	public void setSkipNextStation(short skipNextStation) {
		this.skipNextStation = skipNextStation;
	}

	public int getSectionRunLevel() {
		return sectionRunLevel;
	}

	public void setSectionRunLevel(int sectionRunLevel) {
		this.sectionRunLevel = sectionRunLevel;
	}

	public short getDetainCmd() {
		return detainCmd;
	}

	public void setDetainCmd(short detainCmd) {
		this.detainCmd = detainCmd;
	}

	public short getReturnCmd() {
		return returnCmd;
	}

	public void setReturnCmd(short returnCmd) {
		this.returnCmd = returnCmd;
	}

	public short getGotoRailYard() {
		return gotoRailYard;
	}

	public void setGotoRailYard(short gotoRailYard) {
		this.gotoRailYard = gotoRailYard;
	}

	public short getDoorControl() {
		return doorControl;
	}

	public void setDoorControl(short doorControl) {
		this.doorControl = doorControl;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

}
*/