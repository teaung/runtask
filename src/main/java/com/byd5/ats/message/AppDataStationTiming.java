package com.byd5.ats.message;

/**
 * ATS->客户端发送发车倒计时消息体
 * @author wu.xianglan
 *
 */
public class AppDataStationTiming {

	private Integer station_id;			//站台ID
	private long time;					//站停倒计时时间
	
	public Integer getStation_id() {
		return station_id;
	}
	public void setStation_id(Integer station_id) {
		this.station_id = station_id;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	
}
