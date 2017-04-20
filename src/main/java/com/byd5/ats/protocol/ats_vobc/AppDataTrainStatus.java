package com.byd5.ats.protocol.ats_vobc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 列车状态信息帧（VOBC->ATS）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 250ms周期发送
 * 通信超时中断时间为8s
 */
/*
// cu_pub.h
// b. 列车信息帧
typedef struct _vobc2ats_train_msg
{
	uint16_t  train_identify_id;        // 列车识别号VID 15-14bit,预留 13-2bit，列车编号 1-0bit，车编号
	uint16_t  line_num;                 // 线路编号
	uint8_t   train_locate_status;      // 列车定位状态 有定位：0x55 无定位：0xAA
	uint8_t   direction_train;          // 运行方向 上行 0x55 下行 0xAA 其他 0xFF
	uint8_t   wheel_steer;              // 车轮转向   车轮正转/不动 0x55 车轮反转 0xaa
	uint16_t  t_head_track_id;          // 车头估计位置所在逻辑区段ID
	uint32_t  t_head_track_offset;      // 车头估计位置所在逻辑区段偏移量
	uint16_t  t_tail_track_id;          // 车尾估计位置所在逻辑区段ID
	uint32_t  t_tail_track_offset;      // 车尾估计位置所在逻辑区段偏移量
	uint16_t  excessive_error;          // 过读测距误差
	uint16_t  owe_error;                // 欠读测距误差
	uint8_t   atp_mode;                 // 车载ATP当前模式 RM模式：0x01 CM模式：0x02 AM模式：0x03
	uint8_t   train_run_mode;           // 车载ATP当前模式 CBTC：0x01 点式：0x02 联锁：0x03
	uint8_t   train_comp;               // 列车完整性 完整：0x55 不完整：0xAA
	uint8_t   train_emerg;              // 列车紧急制动状态  紧急制动：0x55 无紧急制动：0xAA
	uint8_t   train_ar_status;          // 列车AR状态 	AR状态：0x55 非AR状态:0xAA 
	uint16_t  train_speed;              // 列车速度信息
	uint8_t   train_door;               // 车门状态  开门：0x55 关门：0xAA 门旁路：0xFF
	uint8_t   park_stab_status;         // 列车停稳状态  停稳且停准：0x55 未停稳/未停准：0xAA
	uint8_t   park_ensure_status;       // 停车保证状态   可停车：0x55 无法停车：0xAA 无效：0xFF
	uint8_t   turnback_none_sta;        // 无人折返状态   无人折返折入中：0x55 无人折返折出中：0xAA 未在无人折返中：0x00
	uint8_t   pre_choose_mode;          // 预选模式  CBTC-AM：0x01 CBTC-CM:0x02 ITC-AM：0x03 ITC-CM:0x04 IL-RM:0x05
	uint8_t   stop_emerg_reason;        // 紧急制动原因 
	uint16_t  stop_emerg_speed;         // 当前紧急制动触发速度    无效值：0xFFFF
	uint16_t  adv_speed;                // 当前推荐速度    无效值：0xFFFF
	uint16_t  track_id1;                // 轨道区段ID
	uint32_t  track_offset1;            // 区段内偏移量
	uint16_t  track_id2;                // 轨道区段ID
	uint32_t  track_offset2;            // 区段内偏移量
	uint32_t  reserv;                   // 预留 ？？？ 
}vobc2ats_train_msg_t;

 */
public class AppDataTrainStatus {

	/*
	 * 消息类型（2字节）：0x0204=列车位置报告及工作模式信息（VOBC->ATS）
	 */
	//private short type;
	
	/*
	 * 列车识别号VID（2字节）：15-14bit=预留； 13-2bit=列车编号； 1-0bit=车编号；
	 */
	@JsonProperty("train_identify_id")
	public short  trainCode;        // 列车识别号VID 15-14bit：预留； 13-2bit：列车编号； 1-0bit：车编号；
	
	/*
	 * 线路编号（2字节）：全网统一标识
	 */
	@JsonProperty("line_num")
	public short  lineNum;                 // 线路编号
	
	/*
	 * 列车定位状态（1字节）：有定位=0x55； 无定位=0xAA；
	 */
	@JsonProperty("train_locate_status")
	public short  trainPosition;      // 列车定位状态 有定位：0x55 无定位：0xAA
	
	/*
	 * 列车运行方向（1字节）：上行=0x55； 下行=0xAA；其他=0xFF；
	 */
	@JsonProperty("direction_train")
	public short  runDirection;          // 运行方向 上行 0x55 下行 0xAA 其他 0xFF
	
	/*
	 * 车轮转向（1字节）：车轮正转（前进）=0x55； 车轮反转（后退）=0xAA；
	 * 车轮不转动时按正转发送
	 */
	@JsonProperty("wheel_steer")
	public short wheerSteering;              // 车轮转向   车轮正转/不动 0x55 车轮反转 0xaa
	
	/*
	 * 车头估计位置所在逻辑区段ID（2字节）：
	 */
	@JsonProperty("t_head_track_id")
	public int  trainHeaderAtLogic;          // 车头估计位置所在逻辑区段ID
	/*
	 * 车头估计位置所在逻辑区段内偏移量（4字节）：单位cm
	 */
	@JsonProperty("t_head_track_offset")
	public int  trainHeaderAtLogicOffset;      // 车头估计位置所在逻辑区段偏移量
	/*
	 * 车尾估计位置所在逻辑区段ID（2字节）：
	 */
	@JsonProperty("t_tail_track_id")
	public int  trainTailAtLogic;          // 车尾估计位置所在逻辑区段ID
	/*
	 * 车尾估计位置所在逻辑区段内偏移量（4字节）：单位cm
	 */
	@JsonProperty("t_tail_track_offset")
	public int  trainTailAtLogicOffset;      // 车尾估计位置所在逻辑区段偏移量
	
	/*
	 * 过读测距误差（2字节）：车头估计位置到最小安全前端的距离，单位cm
	 */
	@JsonProperty("excessive_error")
	public int  moreReadRangeError;          // 过读测距误差
	/*
	 * 欠读测距误差（2字节）：车头估计位置到最小安全前端的距离，单位cm
	 */
	@JsonProperty("owe_error")
	public int  lessReadRangeError;                // 欠读测距误差
	
	/*
	 * ATP当前模式（1字节）：RM模式=0x01； CM模式=0x02； AM模式=0x03；
	 */
	@JsonProperty("atp_mode")
	public byte   atpMode;                 // 车载ATP当前模式 RM模式：0x01 CM模式：0x02 AM模式：0x03
	/*
	 * 车载工作模式（1字节）：CBTC=0x01； 点式=0x02；联锁=0x03；
	 */
	@JsonProperty("train_run_mode")
	public byte   vobcWorkMode;           // 车载工作模式 CBTC：0x01 点式：0x02 联锁：0x03

	/*
	 * 列车完整性（1字节）：完整=0x55； 不完整=0xAA；
	 */
	@JsonProperty("train_comp")
	public short  trainComplete;               // 列车完整性 完整：0x55 不完整：0xAA
	/*
	 * 列车紧急制动状态（1字节）：紧急制动=0x55； 无紧急制动=0xAA；
	 */
	@JsonProperty("train_emerg")
	public short  trainEmergencyBrake;              // 列车紧急制动状态  紧急制动：0x55 无紧急制动：0xAA
	/*
	 * 列车AR状态（1字节）：AR状态=0x55； 非AR状态=0xAA；
	 * （列车自动折返状态）列车换端过程中，原尾端ATP与ZC建立通信后到升级之前的状态
	 */
	@JsonProperty("train_ar_status")
	public short  trainAutoReturnStatus;          // 列车AR状态 	AR状态：0x55 非AR状态:0xAA 
	
	/*
	 * 列车速度信息（2字节）：单位cm/s
	 */
	@JsonProperty("train_speed")
	public short  trainSpeed;              // 列车速度信息
	
	/*
	 * 车门状态（1字节）：开门=0x55； 关门=0xAA；门旁路=0xFF；
	 */
	@JsonProperty("train_door")
	public short  trainDoor;               // 车门状态  开门：0x55 关门：0xAA 门旁路：0xFF
	/*
	 * 列车停稳状态（1字节）：停稳且停准=0x55； 未停稳/未停准=0xAA；
	 */
	@JsonProperty("park_stab_status")
	public short  trainPark;         // 列车停稳状态  停稳且停准：0x55 未停稳/未停准：0xAA
	/*
	 * 停车保证状态（1字节）：可停车=0x55； 无法停车=0xAA；无效=0xFF；
	 */
	@JsonProperty("park_ensure_status")
	public short  parkEnsure;       // 停车保证状态   可停车：0x55 无法停车：0xAA 无效：0xFF

	/*
	 * 无人折返状态（1字节）：无人折返折入中=0x55； 无人折返折出中=0xAA；未在无人折返中=0x00；
	 * 若车载ATP无相关功能，则发送无效值；
	 * 若ATS无此功能，则可不处理此信息；
	 */
	@JsonProperty("turnback_none_sta")
	public short  nobodyReturnStatus;        // 无人折返状态   无人折返折入中：0x55 无人折返折出中：0xAA 未在无人折返中：0x00
	
	/*
	 * 预选模式（1字节）：CBTC-AM=0x01； CBTC-CM=0x02；ITC-AM=0x03；ITC-CM=0x04；IL-RM=0x05；
	 */
	@JsonProperty("pre_choose_mode")
	public byte   preselectionMode;          // 预选模式  CBTC-AM：0x01 CBTC-CM:0x02 ITC-AM：0x03 ITC-CM:0x04 IL-RM:0x05
	
	/*
	 * 紧急制动原因（1字节）：根据业主要求定义
	 */
	@JsonProperty("stop_emerg_reason")
	public byte   eBrakeReason;        // 紧急制动原因 
	
	/*
	 * 当前紧急制动触发速度（2字节）：无效值=0xFFFF；
	 * 单位cm/s；ATP曲线对应的EBI速度；
	 * 当VOBC无法发送此信息时就发送无效值；
	 */
	@JsonProperty("stop_emerg_speed")
	public int  eBrakeTriggerSpeed;         // 当前紧急制动触发速度    无效值：0xFFFF
	/*
	 * 当前推荐速度（2字节）：无效值=0xFFFF；
	 * 单位cm/s；ATO曲线对应的常用制动速度；
	 * 当VOBC无法发送此信息时就发送无效值；
	 */
	@JsonProperty("adv_speed")
	public int  advisorySpeed;                // 当前推荐速度    无效值：0xFFFF

	/*
	 * 轨道区段ID（2字节）：
	 */
	@JsonProperty("track_id1")
	public int  trackId1;                // 轨道区段ID
	/*
	 * 轨道区段内偏移量（4字节）：单位cm
	 */
	@JsonProperty("track_offset1")
	public int  trackOffset1;            // 区段内偏移量
	/*
	 * 轨道区段ID（2字节）：
	 */
	@JsonProperty("track_id2")
	public int  trackId2;                // 轨道区段ID
	/*
	 * 轨道区段内偏移量（4字节）：单位cm
	 */
	@JsonProperty("track_offset2")
	public int  trackOffset2;            // 区段内偏移量
	
	/*
	 * 预留（4字节）：
	 */
	@JsonProperty("reserv")
	public int  reserved;                   // 预留 ？？？ 
	
/*	
	public short getTrain_identify_id() {
		return train_identify_id;
	}
	public void setTrain_identify_id(short train_identify_id) {
		this.train_identify_id = train_identify_id;
	}
	public short getLine_num() {
		return line_num;
	}
	public void setLine_num(short line_num) {
		this.line_num = line_num;
	}
	public byte getTrain_locate_status() {
		return train_locate_status;
	}
	public void setTrain_locate_status(byte train_locate_status) {
		this.train_locate_status = train_locate_status;
	}
	public byte getDirection_train() {
		return direction_train;
	}
	public void setDirection_train(byte direction_train) {
		this.direction_train = direction_train;
	}
	public byte getWheel_steer() {
		return wheel_steer;
	}
	public void setWheel_steer(byte wheel_steer) {
		this.wheel_steer = wheel_steer;
	}
	
	public short getT_head_track_id() {
		return t_head_track_id;
	}
	public void setT_head_track_id(short t_head_track_id) {
		this.t_head_track_id = t_head_track_id;
	}
	public int getT_head_track_offset() {
		return t_head_track_offset;
	}
	public void setT_head_track_offset(int t_head_track_offset) {
		this.t_head_track_offset = t_head_track_offset;
	}
	public short getT_tail_track_id() {
		return t_tail_track_id;
	}
	public void setT_tail_track_id(short t_tail_track_id) {
		this.t_tail_track_id = t_tail_track_id;
	}
	public int getT_tail_track_offset() {
		return t_tail_track_offset;
	}
	public void setT_tail_track_offset(int t_tail_track_offset) {
		this.t_tail_track_offset = t_tail_track_offset;
	}
	public short getExcessive_error() {
		return excessive_error;
	}
	public void setExcessive_error(short excessive_error) {
		this.excessive_error = excessive_error;
	}
	public short getOwe_error() {
		return owe_error;
	}
	public void setOwe_error(short owe_error) {
		this.owe_error = owe_error;
	}
	public byte getAtp_mode() {
		return atp_mode;
	}
	public void setAtp_mode(byte atp_mode) {
		this.atp_mode = atp_mode;
	}
	public byte getTrain_run_mode() {
		return train_run_mode;
	}
	public void setTrain_run_mode(byte train_run_mode) {
		this.train_run_mode = train_run_mode;
	}
	public byte getTrain_comp() {
		return train_comp;
	}
	public void setTrain_comp(byte train_comp) {
		this.train_comp = train_comp;
	}
	public byte getTrain_emerg() {
		return train_emerg;
	}
	public void setTrain_emerg(byte train_emerg) {
		this.train_emerg = train_emerg;
	}
	public byte getTrain_ar_status() {
		return train_ar_status;
	}
	public void setTrain_ar_status(byte train_ar_status) {
		this.train_ar_status = train_ar_status;
	}
	public short getTrain_speed() {
		return train_speed;
	}
	public void setTrain_speed(short train_speed) {
		this.train_speed = train_speed;
	}
	public byte getTrain_door() {
		return train_door;
	}
	public void setTrain_door(byte train_door) {
		this.train_door = train_door;
	}
	public byte getPark_stab_status() {
		return park_stab_status;
	}
	public void setPark_stab_status(byte park_stab_status) {
		this.park_stab_status = park_stab_status;
	}
	public byte getPark_ensure_status() {
		return park_ensure_status;
	}
	public void setPark_ensure_status(byte park_ensure_status) {
		this.park_ensure_status = park_ensure_status;
	}
	public byte getTurnback_none_sta() {
		return turnback_none_sta;
	}
	public void setTurnback_none_sta(byte turnback_none_sta) {
		this.turnback_none_sta = turnback_none_sta;
	}
	public byte getPre_choose_mode() {
		return pre_choose_mode;
	}
	public void setPre_choose_mode(byte pre_choose_mode) {
		this.pre_choose_mode = pre_choose_mode;
	}
	public byte getStop_emerg_reason() {
		return stop_emerg_reason;
	}
	public void setStop_emerg_reason(byte stop_emerg_reason) {
		this.stop_emerg_reason = stop_emerg_reason;
	}
	public short getStop_emerg_speed() {
		return stop_emerg_speed;
	}
	public void setStop_emerg_speed(short stop_emerg_speed) {
		this.stop_emerg_speed = stop_emerg_speed;
	}
	
	public short getAdv_speed() {
		return adv_speed;
	}
	public void setAdv_speed(short adv_speed) {
		this.adv_speed = adv_speed;
	}
	
	public short getTrack_id1() {
		return track_id1;
	}
	public void setTrack_id1(short track_id1) {
		this.track_id1 = track_id1;
	}
	public int getTrack_offset1() {
		return track_offset1;
	}
	public void setTrack_offset1(int track_offset1) {
		this.track_offset1 = track_offset1;
	}

	public short getTrack_id2() {
		return track_id2;
	}
	public void setTrack_id2(short track_id2) {
		this.track_id2 = track_id2;
	}
	public int getTrack_offset2() {
		return track_offset2;
	}
	public void setTrack_offset2(int track_offset2) {
		this.track_offset2 = track_offset2;
	}

	public int getReserv() {
		return reserv;
	}
	public void setReserv(int reserv) {
		this.reserv = reserv;
	}*/
	
}
