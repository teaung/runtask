package com.byd5.ats.message;

import org.springframework.beans.factory.annotation.Value;

/*
[rungraph] Received 
'{"traingroupnum":3,"tablenum":1,"trainnum":102,
"rungraphTodayTimetable":[
{"platformId":28673,"planArriveTime":1493191186000,"planLeaveTime":1493191246000,"skip":false,"returnMode":0,"prevPlatformId":28674,"nextPlatformId":28675},
{"platformId":28675,"planArriveTime":1493191336000,"planLeaveTime":1493191396000,"skip":false,"returnMode":0,"prevPlatformId":28673,"nextPlatformId":28677},
{"platformId":28677,"planArriveTime":1493191486000,"planLeaveTime":1493191546000,"skip":false,"returnMode":0,"prevPlatformId":28675,"nextPlatformId":28679},
{"platformId":28679,"planArriveTime":1493191636000,"planLeaveTime":1493191696000,"skip":false,"returnMode":0,"prevPlatformId":28677,"nextPlatformId":28681},
{"platformId":28681,"planArriveTime":1493191786000,"planLeaveTime":1493191846000,"skip":false,"returnMode":0,"prevPlatformId":28679,"nextPlatformId":28683},
{"platformId":28683,"planArriveTime":1493191966000,"planLeaveTime":1493192026000,"skip":false,"returnMode":0,"prevPlatformId":28681,"nextPlatformId":28684},
{"platformId":28684,"planArriveTime":1493192086000,"planLeaveTime":1493192146000,"skip":false,"returnMode":1,"prevPlatformId":28683,"nextPlatformId":28683}
]}'
*/
public class TrainRunTimetable {
	
	public String servTag = "";
	
	private int platformId;// 站台ID
	private long planArriveTime;// 计划到站时间
	private long planLeaveTime;// 计划离站时间
	private boolean skip;// 是否跳停
	private byte returnMode;// 折返模式(0=无折返；1=站前折返；2=站后折返)
	private int prevPlatformId;// 上一站台ID
	private int nextPlatformId;// 下一站台ID

	public int getPlatformId() {
		return platformId;
	}
	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}
	public long getPlanArriveTime() {
		return planArriveTime;
	}
	public void setPlanArriveTime(long planArriveTime) {
		this.planArriveTime = planArriveTime;
	}
	public long getPlanLeaveTime() {
		return planLeaveTime;
	}
	public void setPlanLeaveTime(long planLeaveTime) {
		this.planLeaveTime = planLeaveTime;
	}
	public boolean isSkip() {
		return skip;
	}
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	public byte getReturnMode() {
		return returnMode;
	}
	public void setReturnMode(byte returnMode) {
		this.returnMode = returnMode;
	}
	public int getPrevPlatformId() {
		return prevPlatformId;
	}
	public void setPrevPlatformId(int prevPlatformId) {
		this.prevPlatformId = prevPlatformId;
	}
	public int getNextPlatformId() {
		return nextPlatformId;
	}
	public void setNextPlatformId(int nextPlatformId) {
		this.nextPlatformId = nextPlatformId;
	}
}
