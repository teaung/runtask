package com.byd5.ats.message;

/**
 * 数据库保存的停站时间
 * @author wu.xianglan
 *
 */
public class DwellTimeData {

	private int platformId;
	
	private int time;
	
	private int setWay;

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getPlatformId() {
		return platformId;
	}

	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}

	public int getSetWay() {
		return setWay;
	}

	public void setSetWay(int setWay) {
		this.setWay = setWay;
	}
	
}
