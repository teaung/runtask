package com.byd5.ats.protocol.ats_vobc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 车载设备报警信息帧（VOBC->ATS）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 250ms周期发送
 */
/*
// cu_pub.h
// c. 车载设备报警信息帧
typedef struct _vobc2ats_train_warning
{
	uint16_t  train_identify_id;        // 列车识别号VID   15-14bit,预留 13-2bit，列车编号 1-0bit，车编号
	uint8_t  ato_break;                 // ATO故障    有故障：0x55 无故障:0xAA
	uint8_t  btm_break;                 // BTM故障   有故障：0x55 无故障:0xAA
	uint8_t  inface_break;              // 车载人机界面故障      有故障：0x55 无故障:0xAA
	uint8_t  radar_break;               // 雷达故障   有故障：0x55 无故障:0xAA
	uint8_t  mana_sys_break;            // 与列车信息管理系统通信故障  有故障：0x55 无故障:0xAA
	uint8_t  meas_spe_sen_break;        // 测速传感器故障   有故障：0x55 无故障:0xAA 
	uint8_t  acc_spe_break;             // 加速度计故障  有故障：0x55 无故障:0xAA 
	uint8_t  atp_break;                 // ATP故障   有故障：0x55 无故障:0xAA 
	uint8_t  mainboard_break;           // 板卡信息  ??? 
}vobc2ats_train_warning_t;

 */
public class AppDataTrainAlert {

	/*
	 * 消息类型（2字节）：0x0206=车载设备报警信息（VOBC->ATS）
	 */
	//private short type;
	
	/*
	 * 列车识别号VID（2字节）：15-14bit=预留； 13-2bit=列车编号； 1-0bit=车编号；
	 */
	@JsonProperty("train_identify_id")
	public short  trainCode;          // 列车识别号VID   15-14bit,预留 13-2bit，列车编号 1-0bit，车编号
	/*
	 * ATO故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("ato_break")
	public short  ato;                 // ATO故障    有故障：0x55 无故障:0xAA
	/*
	 * BTM故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("btm_break")
	public short  btm;                 // BTM故障   有故障：0x55 无故障:0xAA
	/*
	 * 车载人机界面故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("inface_break")
	public short  dmi;              // 车载人机界面故障      有故障：0x55 无故障:0xAA
	/*
	 * 雷达故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("radar_break")
	public short  radar;               // 雷达故障   有故障：0x55 无故障:0xAA
	/*
	 * 与列车信息管理系统通信故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("mana_sys_break")
	public short  comm2info;            // 与列车信息管理系统通信故障  有故障：0x55 无故障:0xAA
	/*
	 * 测速传感器故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("meas_spe_sen_break")
	public short  speedSensor;        // 测速传感器故障   有故障：0x55 无故障:0xAA 
	/*
	 * 加速度计故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("acc_spe_break")
	public short  accSensor;             // 加速度计故障  有故障：0x55 无故障:0xAA 
	/*
	 * ATP故障（1字节）：有故障=0x55；无故障=0xAA； 
	 */
	@JsonProperty("atp_break")
	public short  atp;                 // ATP故障   有故障：0x55 无故障:0xAA 
	/*
	 * 板卡信息（6字节）：自定义，在工程项目中具体明确
	 * 为何cu_pub.h中定义为1字节？
	 */
	@JsonProperty("mainboard_break")
	public short  mainboard;           // 板卡信息  ??? 
	

/*	public short getTrain_identify_id() {
		return train_identify_id;
	}
	public void setTrain_identify_id(short train_identify_id) {
		this.train_identify_id = train_identify_id;
	}
	public byte getAto_break() {
		return ato_break;
	}
	public void setAto_break(byte ato_break) {
		this.ato_break = ato_break;
	}
	public byte getBtm_break() {
		return btm_break;
	}
	public void setBtm_break(byte btm_break) {
		this.btm_break = btm_break;
	}
	public byte getInface_break() {
		return inface_break;
	}
	public void setInface_break(byte inface_break) {
		this.inface_break = inface_break;
	}
	public byte getRadar_break() {
		return radar_break;
	}
	public void setRadar_break(byte radar_break) {
		this.radar_break = radar_break;
	}
	public byte getMana_sys_break() {
		return mana_sys_break;
	}
	public void setMana_sys_break(byte mana_sys_break) {
		this.mana_sys_break = mana_sys_break;
	}
	public byte getMeas_spe_sen_break() {
		return meas_spe_sen_break;
	}
	public void setMeas_spe_sen_break(byte meas_spe_sen_break) {
		this.meas_spe_sen_break = meas_spe_sen_break;
	}
	public byte getAcc_spe_break() {
		return acc_spe_break;
	}
	public void setAcc_spe_break(byte acc_spe_break) {
		this.acc_spe_break = acc_spe_break;
	}
	public byte getAtp_break() {
		return atp_break;
	}
	public void setAtp_break(byte atp_break) {
		this.atp_break = atp_break;
	}
	public byte getMainboard_break() {
		return mainboard_break;
	}
	public void setMainboard_break(byte mainboard_break) {
		this.mainboard_break = mainboard_break;
	}*/
}
