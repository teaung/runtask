package com.byd5.ats.protocol.ats_zc;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 上电TSR确认帧（ATS->ZC）
 * 参考：《LRTSW-SYS-ATS与ZC通信接口协议》
 * 非周期发送
 */
/*
// cu_pub.h
// c. 上电TSR确认
typedef struct _ats2zc_electrify_tsr
{
	uint16_t zc_num;                // 区域编号 
}ats2zc_electrify_tsr_t;

 */
public class AppDataZCBootTSRAck {

	/*
	 * 消息类型（2字节）：0x0209=上电TSR确认信息（ATS->ZC）
	 */
	//private short type;

	/*
	 * ZC编号（2字节）：
	 */
	@JsonProperty("zc_num")
	public short zcId;       // 区域编号 

/*	public short getZc_num() {
		return zc_num;
	}
	public void setZc_num(short zc_num) {
		this.zc_num = zc_num;
	}*/

}
