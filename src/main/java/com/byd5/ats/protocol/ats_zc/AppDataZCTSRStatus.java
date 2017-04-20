package com.byd5.ats.protocol.ats_zc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * TSR状态帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 250ms周期发送
 * ZC向ATS周期报告全部逻辑区段的限速执行状态。该信息作为TSR安全连接ZC->ATS方向的心跳信息。
 */
/*
| 序号 | 参数名 | 取值 | 说明|
|---|---|---|---|
| 1 | T<sub>ZCCycle</sub> | 250ms | ZC发送应用层消息的周期时间值暂定250ms |
| 2 | T<sub>ATSCycle</sub>| 250ms | ATS发送应用层消息的周期时间值暂定250ms |
| 3 | T<sub>ATSTimeout</sub> | 6s | ZC 应用层判断通信超时中断的时间 |
| 4 | T<sub>ZCTimeout</sub>| 6s | ATS应用层判断通信超时中断的时间 |
| 5 | T<sub>ATSCmdHold</sub>| 3s | ATS命令维持的时间 |
| 6 | T<sub>ZCCmdHold</sub>| 3s | ZC命令维持的时间 |
 */

/*
// cu_pub.h
// c. TSR状态信息帧
typedef struct _zc2ats_status_tsr
{
	uint8_t  tsr_electrify_confirm;  // 上电TSR确认请求 有效 0x55 无效 0xAA
	uint16_t logic_track_num;        // 逻辑区段数量
}zc2ats_status_tsr_t;

 */
public class AppDataZCTSRStatus {

	/*
	 * 消息类型（2字节）：0x0206=TSR状态信息（ZC->ATS）
	 */
	//private short type;
	
	/*
	 * 上电TSR确认请求（1字节）：0x55=有效；0xAA=无效；
	 */
	@JsonProperty("tsr_electrify_confirm")
	public short  zcBootTSRConfirm;        // 上电TSR确认请求 有效 0x55 无效 0xAA
	
	/*
	 * 逻辑区段数量（2字节）：ZC管辖范围内全部逻辑区段的数量
	 */
	@JsonProperty("logic_track_num")
	public short logicNum;      // 逻辑区段数量

	/*
	 * 临时限速信息（1*n字节）：逻辑区段设置的临时限速值，每5km/h一个等级；
	 * 4～16=20km/h～80km/h；
	 * 0xFF=无限速；
	 * 其他=非法值；
	 */
/*	@JsonProperty("lgc_tsr_sta")
	private List<Short> logicTSRValue = new ArrayList<Short>();*/
	//private List<Byte> logic_track_tsr;
	
	
/*	public byte getTsr_electrify_confirm() {
		return tsr_electrify_confirm;
	}
	public void setTsr_electrify_confirm(byte tsr_electrify_confirm) {
		this.tsr_electrify_confirm = tsr_electrify_confirm;
	}

	public short getLogic_track_num() {
		return logic_track_num;
	}
	public void setLogic_track_num(short logic_track_num) {
		this.logic_track_num = logic_track_num;
	}*/
	
/*	public List<Byte> getLogic_track_ids() {
		return logic_track_tsr;
	}
	public void setLogic_track_ids(List<Byte> logic_track_tsr) {
		this.logic_track_tsr = logic_track_tsr;
	}*/
}
