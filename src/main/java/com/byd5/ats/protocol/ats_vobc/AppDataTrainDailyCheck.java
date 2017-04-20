package com.byd5.ats.protocol.ats_vobc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 车载设备日检状态信息帧（VOBC->ATS）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 非周期发送
 */
/*
// cu_pub.h
// d. 车载设备日检状态信息帧
typedef struct _vobc2ats_train_every_check
{
	uint16_t  train_identify_id;        // 列车识别号VID  15-14bit,预留 13-2bit，列车编号 1-0bit，车编号
	uint16_t  every_stutas;             // 	日检状态  ???
}vobc2ats_train_check_t;

 */
public class AppDataTrainDailyCheck {

	/*
	 * 消息类型（2字节）：0x0208=车载设备报警信息（VOBC->ATS）
	 */
	//private short type;
	
	/*
	 * 列车识别号VID（2字节）：15-14bit=预留； 13-2bit=列车编号； 1-0bit=车编号；
	 */
	@JsonProperty("train_identify_id")
	public short  trainCode;         // 列车识别号VID   15-14bit,预留 13-2bit，列车编号 1-0bit，车编号
	/*
	 * 日检状态（6字节）：自定义 
	 * 为何cu_pub.h中定义为2字节？
	 */
	@JsonProperty("every_stutas")
	private short  dailyCheckStatus;              // 	日检状态  ???
	

/*	public short getTrain_identify_id() {
		return train_identify_id;
	}
	public void setTrain_identify_id(short train_identify_id) {
		this.train_identify_id = train_identify_id;
	}

	public short getEvery_stutas() {
		return every_stutas;
	}
	public void setEvery_stutas(short every_stutas) {
		this.every_stutas = every_stutas;
	}*/
}
