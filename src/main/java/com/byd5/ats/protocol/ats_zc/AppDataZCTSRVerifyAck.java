package com.byd5.ats.protocol.ats_zc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * TSR验证命令确认帧（ZC->ATS）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ZC向ATS发送TSR验证命令信息包，用于通知ATS当前ZC对该TSR命令进行可执行性校验的结果
 */
/*
// cu_pub.h
// a. TSR验证命令确认帧
typedef struct _zc2ats_verify_tsr
{
	uint8_t  confirm_result;        // 确认结果 成功 0x55 失败 0xAA
	uint8_t  fail_reason;           // 失败原因
                                    // 无报警信息  0x00 命令参数不合法 0x01 限速区域有重叠 0x02 限速区域与列车MA区域有重叠 0x03
                                           未收到联锁的确认 0x04 验证消息结果超时 0x05 验证取消不存在的限速 0x07 未知原因 0xaa
	uint8_t  temp_lim_v;            // 临时限速信息      每5km/h一个等级，不考虑线路限速  4-16：20km/h-80km/h 无限速：0xFF
	uint16_t logic_track_num;       // 逻辑区段数量

}zc2ats_verify_tsr_t;

 */
public class AppDataZCTSRVerifyAck {

	/*
	 * 消息类型（2字节）：0x0202=TSR验证命令确认信息（ZC->ATS）
	 */
	//private short type;
	
	/*
	 * 确认结果（1字节）：0x55=成功；0xAA=失败；
	 */
	@JsonProperty("confirm_result")
	public short result;        // 确认结果 成功 0x55 失败 0xAA
	
	/*
	 * 失败原因（1字节）：
	 * 0x00=无报警信息；
	 * 0x01=命令参数不合法；
	 * 0x02=限速区域有重叠；
	 * 0x03=限速区域与列车MA区域有重叠；
	 * 0x04=未收到联锁的确认；
	 * 0x05=验证消息结果超时；
	 * 0x07=验证取消不存在的限速；
	 * 0xAA=未知原因；
	 */
	@JsonProperty("fail_reason")
	public short failReason;           // 失败原因
	
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

	public byte getFail_reason() {
		return fail_reason;
	}
	public void setFail_reason(byte fail_reason) {
		this.fail_reason = fail_reason;
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
