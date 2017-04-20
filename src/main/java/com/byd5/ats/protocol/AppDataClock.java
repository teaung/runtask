package com.byd5.ats.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * ATS心跳信息帧（ATS->VOBC/ZC/CI）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 250ms周期发送
 */
/*
// cu_pub.h
// 公用时钟信息结构体
typedef struct _ats_msg_time
{
	uint8_t  year;                      // 年 
	uint8_t  month;                     // 月 
	uint8_t  day;                       // 日 
	uint8_t  hour;                      // 时 
	uint8_t  minute;                    // 分 
	uint8_t  second;                    // 秒 
}ats_msg_time_t;
 */
public class AppDataClock {

	/*
	 * 消息类型（2字节）：0x0201=ATS心跳信息（ATS->VOBC）
	 */
	//private short type;
	
	/*
	 * 年（1字节）：11～199表示2011～2199
	 */
	@JsonProperty("year")
	public short year;
	
	/*
	 * 月（1字节）：1～12
	 */
	@JsonProperty("month")
	public byte month;
	
	/*
	 * 日（1字节）：1～31
	 */
	@JsonProperty("day")
	public byte day;
	
	/*
	 * 小时（1字节）：0～23
	 */
	@JsonProperty("hour")
	public byte hour;
	
	/*
	 * 分钟（1字节）：0～59
	 */
	@JsonProperty("minute")
	public byte minute;

	/*
	 * 秒钟（1字节）：0～59
	 */
	@JsonProperty("second")
	public byte second;
	
	
	
/*	public byte getYear() {
		return year;
	}
	public void setYear(byte year) {
		this.year = year;
	}
	
	public byte getMonth() {
		return month;
	}
	public void setMonth(byte month) {
		this.month = month;
	}
	
	public byte getDay() {
		return day;
	}
	public void setDay(byte day) {
		this.day = day;
	}
	
	public byte getHour() {
		return hour;
	}
	public void setNext_ci_id(byte hour) {
		this.hour = hour;
	}
	
	public byte getMinute() {
		return minute;
	}
	public void setMinute(byte minute) {
		this.minute = minute;
	}
	
	public byte getSecond() {
		return second;
	}
	public void setSecond(byte second) {
		this.second = second;
	}*/
}
