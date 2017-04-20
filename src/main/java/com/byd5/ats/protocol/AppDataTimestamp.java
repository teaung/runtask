package com.byd5.ats.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * VOBC/CI/ZC上报数据的时间戳
 * 
// cu_pub.h
// cu to ats 时钟结构体
typedef struct _cu2ats_time
{
	int32_t  sec;                       // 秒 
	int32_t  usec;                      // 微秒
}cu2ats_time_t;
 */
public class AppDataTimestamp {

	/*
	 * 秒（4字节）：距离1970-01-01 00:00:00的秒数
	 */
	@JsonProperty("sec")
	public long sec;
	
	/*
	 * 微秒（4字节）：
	 */
	@JsonProperty("usec")
	public long usec;
	
/*	public int getSec() {
		return sec;
	}
	public void setSec(int sec) {
		this.sec = sec;
	}
	
	public int getUsec() {
		return usec;
	}
	public void setUsec(int usec) {
		this.usec = usec;
	}*/
	
}
