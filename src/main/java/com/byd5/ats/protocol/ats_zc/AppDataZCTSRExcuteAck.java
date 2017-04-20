package com.byd5.ats.protocol.ats_zc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * TSR执行命令确认帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ZC向ATS发送TSR执行命令，用于通知ATS当前ZC对该TSR命令执行的结果；该命令信息ZC应向ATS持续反馈3秒钟时间。
 */
/*
// cu_pub.h
// b. TSR执行命令确认帧
typedef struct _zc2ats_execute_tsr
{
	uint8_t  confirm_result;        // 确认结果 成功 0x55 失败 0xAA
	uint8_t  warning_msg;           // 报警信息  无报警信息 0x00 命令参数不合法 0x01 限速区域有重叠 0x02  限速区域与列车MA区域有重叠 0x03 验证消息结果超时 0x05 未经过验证的执行请求 0x06
	uint8_t  temp_lim_v;            // 临时限速信息  每5km/h一个等级，不考虑线路限速  4-16：20km/h-80km/h 无限速：0xFF
	uint16_t logic_track_num;       // 逻辑区段数量 

}zc2ats_execute_tsr_t;

 */
public class AppDataZCTSRExcuteAck {

	/*
	 * 消息类型（2字节）：0x0204=TSR执行命令确认信息（ZC->ATS）
	 */
	//private short type;
	
	/*
	 * 确认结果（1字节）：0x55=成功；0xAA=失败；
	 */
	@JsonProperty("confirm_result")
	public short result;        // 确认结果 成功 0x55 失败 0xAA
	
	/*
	 * 报警信息（1字节）：
	 * 0x00=无报警信息；
	 * 0x01=命令参数不合法；
	 * 0x02=限速区域有重叠；
	 * 0x03=限速区域与列车MA区域有重叠；
	 * 0x05=验证消息结果超时；
	 * 0x06=未经过验证的执行请求；
	 */
	@JsonProperty("warning_msg")
	private short  warning;           // 报警信息
	
	/*
	 * 临时限速信息（1字节）：每5km/h一个等级，不考虑线路限速；
	 * 4～16=20km/h～80km/h；
	 * 0xFF=无限速；
	 * 其他=非法值；
	 */
	@JsonProperty("temp_lim_v")
	public short tsrValue;            // 临时限速信息
	
	/*
	 * 逻辑区段数量（2字节）：TSR区域所包含的逻辑区段的数量
	 */
	@JsonProperty("logic_track_num")
	public short tsrLogicNum;       // 逻辑区段数量

	/*
	 * 逻辑区段ID（2*n字节）：TSR起点至终点逻辑区段的ID
	 */
/*	@JsonProperty("logic_track_ids")
	public List<Short> logicId = new ArrayList<Short>();*/
	
/*	
	public byte getConfirm_result() {
		return confirm_result;
	}
	public void setConfirm_result(byte confirm_result) {
		this.confirm_result = confirm_result;
	}

	public byte getWarning_msg() {
		return warning_msg;
	}
	public void setWarning_msg(byte warning_msg) {
		this.warning_msg = warning_msg;
	}
	
	public byte getTemp_lim_v() {
		return temp_lim_v;
	}
	public void setTemp_lim_v(byte temp_lim_v) {
		this.temp_lim_v = temp_lim_v;
	}

	public short getLogic_track_num() {
		return logic_track_num;
	}
	public void setLogic_track_num(short logic_track_num) {
		this.logic_track_num = logic_track_num;
	}
	
	public List<Short> getLogic_track_ids() {
		return logic_track_ids;
	}
	public void setLogic_track_ids(List<Short> logic_track_ids) {
		this.logic_track_ids = logic_track_ids;
	}*/
}
