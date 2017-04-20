package com.byd5.ats.protocol.ats_zc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * TSR验证命令帧（ATS->ZC）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 * ATS向ZC发送TSR验证命令信息包，要求ZC对该TSR命令进行可执行性校验。
 * 该命令信息ATS应向ZC持续下达3秒钟时间，直到收到ZC回复的命令应答信息或超时退出。TSR信息为双向有效
 */
/*
// cu_pub.h
// a. TSR验证命令帧 
typedef struct _ats2zc_verify_tsr
{
	uint8_t  temp_lim_v;            // 临时限速信息 每5km/h一个等级，不考虑线路限速  4-16：20km/h-80km/h 无限速：0xFF 
	uint16_t logic_track_num;       // 逻辑区段数量

}ats2zc_verify_tsr_t;

 */
public class AppDataZCTSRVerify {

	/*
	 * 消息类型（2字节）：0x0203=TSR验证命令（ATS->ZC）
	 */
	//private short type;
	
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
	}*/
	
/*	public List<Short> getLogic_track_ids() {
		return logic_track_ids;
	}
	public void setLogic_track_ids(List<Short> logic_track_ids) {
		this.logic_track_ids = logic_track_ids;
	}*/
}
