package com.byd5.ats.message;

/**
 * 战场图设置立即发车返回结果
 * @author wu.xianglan
 *
 */
public class BackDepart2AppData {

	private int runtaskCmdType;
	
	private boolean result;
	
	private String code;
	
	private int platformId;

	public BackDepart2AppData(int runtaskCmdType, boolean result, String code, int platformId){
		this.runtaskCmdType = runtaskCmdType;
		this.result = result;
		this.code = code;
		this.platformId = platformId;
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
	
	
}
