package com.byd5.ats.message;

/**
 * 战场图设置停站时间返回结果信息
 * @author wu.xianglan
 *
 */
public class BackDwellTime2AppData {

	private int runtaskCmdType;
	
	private boolean result;
	
	private String code;
	
	private int platformId;
	
	private int time;
	
	private int setWay;

	public BackDwellTime2AppData(int runtaskCmdType, boolean result, String code, int platformId, int time, int setWay){
		this.runtaskCmdType = runtaskCmdType;
		this.result = result;
		this.code = code;
		this.platformId = platformId;
		this.time = time;
		this.setWay = setWay;
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getRuntaskCmdType() {
		return runtaskCmdType;
	}

	public void setRuntaskCmdType(int runtaskCmdType) {
		this.runtaskCmdType = runtaskCmdType;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
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
