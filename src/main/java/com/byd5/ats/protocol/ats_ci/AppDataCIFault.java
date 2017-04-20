package com.byd5.ats.protocol.ats_ci;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 故障状态信息帧（CI->ATS）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 400ms周期发送
 * 通信超时中断时间为6s
 */
/*
// cu_pub.h
// b.故障状态信息
typedef struct _ci_msg_error
{
	uint8_t  light_broken_wire;         // 灯丝断丝信息　0x55:未断丝 0xaa:断丝
	uint8_t  light_fuse;                // 灯丝熔丝 0x55:未熔丝 0xaa:熔丝
	uint8_t  power_screen;              // 电源屏故障信息 0x55:
	uint8_t  light_close;               // 信号故障关闭 
	uint8_t  ci_warning;                // 联锁设备报警信息 
	uint8_t  track_warning;             // 轨道故障报警 
	uint8_t  ups_fail;                  // UPS电源故障 
	uint8_t  leu_status;                // LEU通信状态 
	uint8_t  ci_zc_comm;                // 联锁ZC通信 
	uint8_t  ci1_status;                // 联锁1工作状态 
	uint8_t  ci2_status;                // 联锁2工作状态 
	uint8_t  ci1_power;                 // 1路电源状态 
	uint8_t  ci2_power;                 // 2路电源状态
	uint8_t  ci_num;                    // 所通信联锁数量 
	uint8_t  ci_ci_comm;                // 与xx联锁通信状态 
	uint8_t  turnback_auto;             // 无人折返状态 
	uint8_t  switch_none;               // 道岔挤岔 
	uint8_t  signal_check_num;          // 照查条件检查数量 
	uint8_t  signal_check_status;       // 照查条件x 
	uint8_t  psd_num;                   // PSD通信数量 
	uint8_t  ci_psd_comm;               // 与PSD x通信状态 
	uint8_t  ci_mode;                   // 模式状态 
	uint8_t  ci_block;                  // 全站封锁状态
	uint8_t  power_on_unlock;           // 上电解锁状态 
}ci_msg_error_t;
 */
public class AppDataCIFault {

	/*
	 * 消息类型（2字节）：0x0204=故障状态信息（CI->ATS）
	 */
	//private short type;
	
	//信息帧长度：CI发送的故障状态信息帧长度
	//private short length;
	
	/*
	 * 灯丝断丝信息（1字节）：0x55=未断丝；0xAA=断丝；
	 */
	@JsonProperty("light_broken_wire")
	public short signalFilamentBurnout;         // 灯丝断丝信息　0x55:未断丝 0xaa:断丝
	/*
	 * 灯丝熔丝信息（1字节）：0x55=未熔丝；0xAA=熔丝；
	 */
	@JsonProperty("light_fuse")
	public short signalFilamentFuse;	// 灯丝熔丝 0x55:未熔丝 0xaa:熔丝
	/*
	 * 电源屏故障信息（1字节）：0x55=未故障；0xAA=故障；
	 */
	@JsonProperty("power_screen")
	public short powerSupplyPanel;              // 电源屏故障信息 0x55:
	/*
	 * 信号故障关闭（1字节）：0x55=未故障报警；0xAA=故障报警；
	 */
	@JsonProperty("light_close")
	public short signalClose;               // 信号故障关闭 
	/*
	 * 联锁设备报警信息（1字节）：0x55=未故障报警；0xAA=故障报警；
	 */
	@JsonProperty("ci_warning")
	public short ciDevice;                // 联锁设备报警信息 
	/*
	 * 轨道故障报警信息（1字节）：0x55=轨道未故障；0xAA=轨道故障；
	 */
	@JsonProperty("track_warning")
	public short track;             // 轨道故障报警 
	/*
	 * UPS电源故障（1字节）：0x55=未故障；0xAA=故障；
	 */
	@JsonProperty("ups_fail")
	public short ups;                  // UPS电源故障 
	/*
	 * LEU通信状态（1字节）：
	 * 0x01=当任意一个LEU设备与联锁通信正常，显示绿灯；
	 * 0x03=当任意一个LEU设备的两个连接与联锁通信均中断，显示红灯；
	 */
	@JsonProperty("leu_status")
	public byte leuStatus;                // LEU通信状态 

	/*
	 * 联锁与ZC通信状态（1字节）：
	 * 0x01=联锁与ZC双网通信均正常；
	 * 0x02=联锁与ZC单网通信中断；
	 * 0x03=联锁与ZC双网通信都中断；
	 */
	@JsonProperty("ci_zc_comm")
	public byte ci2zcCommStatus;                // 联锁ZC通信 
	/*
	 * 联锁1系工作状态（1字节）：
	 * 0x01=联锁1系主用；
	 * 0x02=联锁1系备用；
	 * 0x03=联锁1系停机或未与其建立通信；
	 */
	@JsonProperty("ci1_status")
	public byte ci1Status;                // 联锁1工作状态 
	/*
	 * 联锁2系工作状态（1字节）：
	 * 0x01=联锁2系主用；
	 * 0x02=联锁2系备用；
	 * 0x03=联锁2系停机或未与其建立通信；
	 */
	@JsonProperty("ci2_status")
	public byte ci2Status;                // 联锁2工作状态
	
	/*
	 * 1路电源状态（1字节）：0x55=1路电源供电；0xAA=1路电源不供电；
	 */
	@JsonProperty("ci1_power")
	public short ci1Power;                 // 1路电源状态 
	/*
	 * 2路电源状态（1字节）：0x55=2路电源供电；0xAA=2路电源不供电；
	 */
	@JsonProperty("ci2_power")
	public short ci2Power;                 // 2路电源状态
	
	/*
	 * 所通信的联锁数量（1字节）：本站联锁实际通信的临站联锁数量；
	 */
	@JsonProperty("ci_num")
	public byte ciNum;                    // 所通信联锁数量 

	/*
	 * 与XX联锁通信状态（1字节）：根据实际连接的联锁数量n，增加相应的联锁通信状态
	 * 0x01=与XX联锁双网通信均正常；
	 * 0x02=与XX联锁单网通信中断；
	 * 0x03=与XX联锁双网通信都中断；
	 */
	@JsonProperty("ci_ci_comm")
	public byte ci2ciCommStatus;                // 与xx联锁通信状态 
	
	/*
	 * 无人折返状态（1字节）：
	 * 0x01=常态；
	 * 0x02=收到ZC的无人折返闪灯命令；
	 * 0x03=收到ZC的无人折返稳灯命令；
	 */
	@JsonProperty("turnback_auto")
	public byte nobodyReturnStatus;             // 无人折返状态 

	/*
	 * 道岔挤岔（1字节）：有任一道岔出现挤岔时，该字段为0xAA；
	 * 0x55=未出现道岔挤岔；
	 * 0xAA=有道岔出现挤岔；
	 */
	@JsonProperty("switch_none")
	public short switchSplit;               // 道岔挤岔
	
	/*
	 * 照查条件检查数量（1字节）：实际连接联锁需要检查照查条件的个数
	 */
	@JsonProperty("signal_check_num")
	public short checkConditionNum;          // 照查条件检查数量 
	/*
	 * 照查条件X（1字节）：根据实际工程照查条件检查个数n及顺序增加或删除
	 * 0x55=对应照查继电器状态吸起；
	 * 0xAA=对应照查继电器落下；
	 */
	@JsonProperty("signal_check_status")
	public short checkConditionStatus;       // 照查条件x
	
	/*
	 * PSD通信数量（1字节）：CI实际通信控制的PSD数量
	 */
	@JsonProperty("psd_num")
	public short psdNum;                   // PSD通信数量 
	/*
	 * 与PSD X通信状态（1字节）：根据实际通信的PSD数量及顺序填写
	 * 0x55=通信正常；
	 * 0xAA=通信中断；
	 */
	@JsonProperty("ci_psd_comm")
	public short ci2psdCommStatus;         // 与PSD x通信状态 

	/*
	 * 模式状态（1字节）：0x55=站控；0xAA=中控；
	 */
	@JsonProperty("ci_mode")
	public short mode;                   // 模式状态 
	/*
	 * 全站封锁状态（1字节）：0x55=未封锁；0xAA=封锁；
	 */
	@JsonProperty("ci_block")
	public short allStationBlock;                  // 全站封锁状态
	/*
	 * 上电解锁状态（1字节）：0x55=未解锁；0xAA=解锁；
	 */
	@JsonProperty("power_on_unlock")
	public short powerOnUnlock;           // 上电解锁状态 
	
/*
	public byte getLight_broken_wire() {
		return light_broken_wire;
	}
	public void setLight_broken_wire(byte light_broken_wire) {
		this.light_broken_wire = light_broken_wire;
	}

	public byte getLight_fuse() {
		return light_fuse;
	}
	public void setLight_fuse(byte light_fuse) {
		this.light_fuse = light_fuse;
	}
	
	public byte getPower_screen() {
		return power_screen;
	}
	public void setPower_screen(byte power_screen) {
		this.power_screen = power_screen;
	}

	public byte getLight_close() {
		return light_close;
	}
	public void setLight_close(byte light_close) {
		this.light_close = light_close;
	}
	
	public byte getCi_warning() {
		return ci_warning;
	}
	public void setCi_warning(byte ci_warning) {
		this.ci_warning = ci_warning;
	}

	public byte getTrack_warning() {
		return track_warning;
	}
	public void setTrack_warning(byte track_warning) {
		this.track_warning = track_warning;
	}
	
	public byte getUps_fail() {
		return ups_fail;
	}
	public void setUps_fail(byte ups_fail) {
		this.ups_fail = ups_fail;
	}
	
	public byte getLeu_status() {
		return leu_status;
	}
	public void setLeu_status(byte leu_status) {
		this.leu_status = leu_status;
	}
	
	public byte getCi_zc_comm() {
		return ci_zc_comm;
	}
	public void setCi_zc_comm(byte ci_zc_comm) {
		this.ci_zc_comm = ci_zc_comm;
	}

	public byte getCi1_status() {
		return ci1_status;
	}
	public void setCi1_status(byte ci1_status) {
		this.ci1_status = ci1_status;
	}
	
	public byte getCi2_status() {
		return ci2_status;
	}
	public void setCi2_status(byte ci2_status) {
		this.ci2_status = ci2_status;
	}
	
	public byte getCi1_power() {
		return ci1_power;
	}
	public void setCi1_power(byte ci1_power) {
		this.ci1_power = ci1_power;
	}
	
	public byte getCi2_power() {
		return ci2_power;
	}
	public void setCi2_power(byte ci2_power) {
		this.ci2_power = ci2_power;
	}

	public byte getCi_num() {
		return ci_num;
	}
	public void setCi_num(byte ci_num) {
		this.ci_num = ci_num;
	}
	
	public byte getCi_ci_comm() {
		return ci_ci_comm;
	}
	public void setCi_ci_comm(byte ci_ci_comm) {
		this.ci_ci_comm = ci_ci_comm;
	}
	
	public byte getTurnback_auto() {
		return turnback_auto;
	}
	public void setTurnback_auto(byte turnback_auto) {
		this.turnback_auto = turnback_auto;
	}
	
	public byte getSwitch_none() {
		return switch_none;
	}
	public void setSwitch_none(byte switch_none) {
		this.switch_none = switch_none;
	}
	
	public byte getSignal_check_num() {
		return signal_check_num;
	}
	public void setSignal_check_num(byte signal_check_num) {
		this.signal_check_num = signal_check_num;
	}
	
	public byte getSignal_check_status() {
		return signal_check_status;
	}
	public void setSignal_check_status(byte signal_check_status) {
		this.signal_check_status = signal_check_status;
	}
	
	public byte getPsd_num() {
		return psd_num;
	}
	public void setPsd_num(byte psd_num) {
		this.psd_num = psd_num;
	}
	
	public byte getCi_psd_comm() {
		return ci_psd_comm;
	}
	public void setCi_psd_comm(byte ci_psd_comm) {
		this.ci_psd_comm = ci_psd_comm;
	}

	public byte getCi_mode() {
		return ci_mode;
	}
	public void setCi_mode(byte ci_mode) {
		this.ci_mode = ci_mode;
	}
	
	public byte getCi_block() {
		return ci_block;
	}
	public void setCi_block(byte ci_block) {
		this.ci_block = ci_block;
	}
	
	public byte getPower_on_unlock() {
		return power_on_unlock;
	}
	public void setPower_on_unlock(byte power_on_unlock) {
		this.power_on_unlock = power_on_unlock;
	}
*/
}
