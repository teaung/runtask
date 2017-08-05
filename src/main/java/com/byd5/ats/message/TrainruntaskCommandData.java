package com.byd5.ats.message;

/**
 * 运行任务保存客户端设置停站时间命令的实体类
 * @author wu.xianglan
 *
 */
public class TrainruntaskCommandData{

	private Integer id;
	
	private short clientNum;
	
	private String userName;
	
	private short aodCmdType;	//停站时间：158, 立即发车：161
	
	private int platform_id;	//站台ID
	
	private int stopTime;		//停站时间
	
	private int setWay;			//设置方式（0，人工设置；1，自动设置）

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public short getClientNum() {
		return clientNum;
	}

	public void setClientNum(short clientNum) {
		this.clientNum = clientNum;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public short getAodCmdType() {
		return aodCmdType;
	}

	public void setAodCmdType(short aodCmdType) {
		this.aodCmdType = aodCmdType;
	}

	public int getPlatform_id() {
		return platform_id;
	}

	public void setPlatform_id(int platform_id) {
		this.platform_id = platform_id;
	}

	public int getStopTime() {
		return stopTime;
	}

	public void setStopTime(int stopTime) {
		this.stopTime = stopTime;
	}

	public int getSetWay() {
		return setWay;
	}

	public void setSetWay(int setWay) {
		this.setWay = setWay;
	}
}
