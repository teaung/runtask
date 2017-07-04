package com.byd5.ats.message;


public class TrainEventPosition {
	
	public String servTag;
	public short serviceNum;
	public short lineNum;					//线路编号（2字节）：全网统一标识
	public short carLineNum;				//车组所属线路编号（2字节）：全网统一标识
	public short carNum;					// 车组号（2字节）：
	public short srcLineNum;				//源线路编号（2字节））：列车始发站线路编号；默认值为0xffff；全网统一标识
	public short trainNum;					//车次号（2字节）：0000～9999；默认值0000
	public short dstLineNum;				//目的地线路编号（2字节）：同线路编号；列车为非计划车时，发送默认值0xffff
	public int dstStationNum;				//目的地号（4字节）：用ASCII码标识，最多4个ASCII码，低于4个时高位用空格补齐；
	public short directionPlan;				//计划运行方向（1字节）：上行=0x55；下行=0xAA
	public Integer station;					//目前所在站台id
	public Integer nextStationId;			//下一停车站台ID（2字节）：定义同“跳停站台ID”，默认值为0xFFFF
	public short  trainPark;         		// 列车停稳状态  停稳且停准：0x55 未停稳/未停准：0xAA
	public String trainHeaderAtphycical;   	//车头所在的物理区段
	private int runningLevel;				//运行等级
	private long timestamp;					//精确到ms
	
	public String getServTag() {
		return servTag;
	}
	public void setServTag(String servTag) {
		this.servTag = servTag;
	}
	public short getServiceNum() {
		return serviceNum;
	}
	public void setServiceNum(short serviceNum) {
		this.serviceNum = serviceNum;
	}
	public short getLineNum() {
		return lineNum;
	}
	public void setLineNum(short lineNum) {
		this.lineNum = lineNum;
	}
	public short getCarLineNum() {
		return carLineNum;
	}
	public void setCarLineNum(short carLineNum) {
		this.carLineNum = carLineNum;
	}
	public short getCarNum() {
		return carNum;
	}
	public void setCarNum(short carNum) {
		this.carNum = carNum;
	}
	public short getSrcLineNum() {
		return srcLineNum;
	}
	public void setSrcLineNum(short srcLineNum) {
		this.srcLineNum = srcLineNum;
	}
	public short getTrainNum() {
		return trainNum;
	}
	public void setTrainNum(short trainNum) {
		this.trainNum = trainNum;
	}
	public short getDstLineNum() {
		return dstLineNum;
	}
	public void setDstLineNum(short dstLineNum) {
		this.dstLineNum = dstLineNum;
	}
	public int getDstStationNum() {
		return dstStationNum;
	}
	public void setDstStationNum(int dstStationNum) {
		this.dstStationNum = dstStationNum;
	}
	public short getDirectionPlan() {
		return directionPlan;
	}
	public void setDirectionPlan(short directionPlan) {
		this.directionPlan = directionPlan;
	}
	public Integer getStation() {
		return station;
	}
	public void setStation(Integer station) {
		this.station = station;
	}
	public Integer getNextStationId() {
		return nextStationId;
	}
	public void setNextStationId(Integer nextStationId) {
		this.nextStationId = nextStationId;
	}
	public short getTrainPark() {
		return trainPark;
	}
	public void setTrainPark(short trainPark) {
		this.trainPark = trainPark;
	}
	public String getTrainHeaderAtphycical() {
		return trainHeaderAtphycical;
	}
	public void setTrainHeaderAtphycical(String trainHeaderAtphycical) {
		this.trainHeaderAtphycical = trainHeaderAtphycical;
	}
	public int getRunningLevel() {
		return runningLevel;
	}
	public void setRunningLevel(int runningLevel) {
		this.runningLevel = runningLevel;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
