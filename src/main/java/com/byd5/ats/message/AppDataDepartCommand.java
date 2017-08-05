package com.byd5.ats.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 战场图设置停站时间命令信息
 * @author wu.xianglan
 *
 */
public class AppDataDepartCommand {

	@JsonProperty("client_num")
	private short clientNum;
	
	@JsonProperty("user_name")
	private String userName;
	
	@JsonProperty("runtaskCmdType")
	private short runtaskCmdType;	//停站时间：158, 立即发车：161
	
	@JsonProperty("platformId")
	private int platformId;	//站台ID
	
	@JsonProperty("groupNum")
	private int groupNum;		//停站时间
			//设置方式（0，人工设置；1，自动设置）	//停站时间时  cmdParameter:[platform_id,time,setWay] //站台ID，停站时间，设置方式（0，人工设置；1，自动设置）
								//立即发车时  cmdParameter:[platform_id,group_mun]  //站台ID，当前站台停车的车组号

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

	public short getRuntaskCmdType() {
		return runtaskCmdType;
	}

	public void setRuntaskCmdType(short runtaskCmdType) {
		this.runtaskCmdType = runtaskCmdType;
	}

	public int getPlatformId() {
		return platformId;
	}

	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}

	public int getGroupNum() {
		return groupNum;
	}

	public void setGroupNum(int groupNum) {
		this.groupNum = groupNum;
	}

}
