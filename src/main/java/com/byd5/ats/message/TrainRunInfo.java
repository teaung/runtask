package com.byd5.ats.message;

/**
 * 列车运行信息
 */

public class TrainRunInfo {
	private int traingroupnum;// 车组号
	private int tablenum;// 表号
	private int trainnum; // 车次号
	private byte runDirection;//运行方向
	private int trainType = 0; // 车次类型，0：计划车，1：非计划车
	private String dstStationNum;//目的地号（4字节）：用ASCII码标识，最多4个ASCII码，低于4个时高位用空格补齐；
	private int lineNum;
	
	private int soonerOrLaterPoint; //早晚点
	private int cabinCrew;//乘务组号|由派班计划确定有哪些乘务组号|
	private int startStation;//起点站id||
	private int endStation;//终点站id||
	private int runningLevel;	//运行等级
	public int driverNum;//司机号

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

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
	public byte getRunDirection() {
		return runDirection;
	}
	public void setRunDirection(byte runDirection) {
		this.runDirection = runDirection;
	}

	public String getDstStationNum() {
		return dstStationNum;
	}

	public void setDstStationNum(String dstStationNum) {
		this.dstStationNum = dstStationNum;
	}

	public int getTrainType() {
		return trainType;
	}

	public void setTrainType(int trainType) {
		this.trainType = trainType;
	}

	public int getSoonerOrLaterPoint() {
		return soonerOrLaterPoint;
	}

	public void setSoonerOrLaterPoint(int soonerOrLaterPoint) {
		this.soonerOrLaterPoint = soonerOrLaterPoint;
	}

	public int getCabinCrew() {
		return cabinCrew;
	}

	public void setCabinCrew(int cabinCrew) {
		this.cabinCrew = cabinCrew;
	}

	public int getStartStation() {
		return startStation;
	}

	public void setStartStation(int startStation) {
		this.startStation = startStation;
	}

	public int getEndStation() {
		return endStation;
	}

	public void setEndStation(int endStation) {
		this.endStation = endStation;
	}

	public int getRunningLevel() {
		return runningLevel;
	}

	public void setRunningLevel(int runningLevel) {
		this.runningLevel = runningLevel;
	}

	public int getDriverNum() {
		return driverNum;
	}

	public void setDriverNum(int driverNum) {
		this.driverNum = driverNum;
	}
	
}
