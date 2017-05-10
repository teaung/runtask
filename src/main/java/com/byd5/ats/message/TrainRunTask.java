package com.byd5.ats.message;

import java.util.List;

//[rungraph] Received '{"traingroupnum":3,"tablenum":1,"trainnum":102,"rungraphTodayTimetable":[{"platformId":28673,"planArriveTime":1493191186000,"planLeaveTime":1493191246000,"skip":false,"returnMode":0,"prevPlatformId":28674,"nextPlatformId":28675},{"platformId":28675,"planArriveTime":1493191336000,"planLeaveTime":1493191396000,"skip":false,"returnMode":0,"prevPlatformId":28673,"nextPlatformId":28677},{"platformId":28677,"planArriveTime":1493191486000,"planLeaveTime":1493191546000,"skip":false,"returnMode":0,"prevPlatformId":28675,"nextPlatformId":28679},{"platformId":28679,"planArriveTime":1493191636000,"planLeaveTime":1493191696000,"skip":false,"returnMode":0,"prevPlatformId":28677,"nextPlatformId":28681},{"platformId":28681,"planArriveTime":1493191786000,"planLeaveTime":1493191846000,"skip":false,"returnMode":0,"prevPlatformId":28679,"nextPlatformId":28683},{"platformId":28683,"planArriveTime":1493191966000,"planLeaveTime":1493192026000,"skip":false,"returnMode":0,"prevPlatformId":28681,"nextPlatformId":28684},{"platformId":28684,"planArriveTime":1493192086000,"planLeaveTime":1493192146000,"skip":false,"returnMode":1,"prevPlatformId":28683,"nextPlatformId":28683}]}'

public class TrainRunTask {
	
	public String servTag = "";
	
	private int traingroupnum = 0;// 车组号
	private int tablenum = 0;// 表号
	private int trainnum = 0; // 车次号
	private List<TrainRunTimetable> rungraphTodayTimetable;// 当日计划运行图时刻表
	
	public int getTraingroupnum() {
		return traingroupnum;
	}
	public void setTraingroupnum(int traingroupnum) {
		this.traingroupnum = traingroupnum;
	}
	public int getTablenum() {
		return tablenum;
	}
	public void setTablenum(int tablenum) {
		this.tablenum = tablenum;
	}
	public int getTrainnum() {
		return trainnum;
	}
	public void setTrainnum(int trainnum) {
		this.trainnum = trainnum;
	}
	public List<TrainRunTimetable> getRungraphTodayTimetable() {
		return rungraphTodayTimetable;
	}
	public void setRungraphTodayTimetable(List<TrainRunTimetable> rungraphTodayTimetable) {
		this.rungraphTodayTimetable = rungraphTodayTimetable;
	}
}
