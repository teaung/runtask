package com.byd5.ats.message;

public class TrainEventPosition {
	public String servTag;// 本服务标识
	public int modeTag;// 1是点式模式，2是CBTC模式
	public long src;// 发送方标识信息
	public int serviceNum;// 表号
	public int lineNum;// 线路编号（2字节）：全网统一标识
	public int cargroupLineNum;// 车组所属线路编号（2字节）：全网统一标识
	public int cargroupNum;// 车组号（2字节）：
	public int srcLineNum;// 源线路编号（2字节））：列车始发站线路编号；默认值为0xffff；全网统一标识
	public int trainNum;// 车次号（2字节）：0000～9999；默认值0000
	public int dstLineNum;// 目的地线路编号（2字节）：同线路编号；列车为非计划车时，发送默认值0xffff
	public String dstCode;// 目的地号（4字节）：用ASCII码标识，最多4个ASCII码，低于4个时高位用空格补齐；
	public short trainDir;// 运行方向（1字节）：上行=0x55；下行=0xAA
	public Integer station;// 目前所在站台id
	public Integer nextStationId;// 下一停车站台ID（2字节）：定义同“跳停站台ID”，默认值为0xFFFF
	public short stopStatus; // 列车停稳状态 停稳且停准：0x55 未停稳/未停准：0xAA
	public String trainHeaderAtLogic;// 车头所在的逻辑区段
	public String trainHeaderAtphysical; // 车头所在的物理区段
	public int runningLevel;// 运行等级
	public int driverNum;// 司机号
	public long timestamp;// 精确到ms
	public String getServTag() {
		return servTag;
	}
	public void setServTag(String servTag) {
		this.servTag = servTag;
	}
	public int getModeTag() {
		return modeTag;
	}
	public void setModeTag(int modeTag) {
		this.modeTag = modeTag;
	}
	public long getSrc() {
		return src;
	}
	public void setSrc(long src) {
		this.src = src;
	}
	public int getServiceNum() {
		return serviceNum;
	}
	public void setServiceNum(int serviceNum) {
		this.serviceNum = serviceNum;
	}
	public int getLineNum() {
		return lineNum;
	}
	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}
	public int getCargroupLineNum() {
		return cargroupLineNum;
	}
	public void setCargroupLineNum(int cargroupLineNum) {
		this.cargroupLineNum = cargroupLineNum;
	}
	public int getCargroupNum() {
		return cargroupNum;
	}
	public void setCargroupNum(int cargroupNum) {
		this.cargroupNum = cargroupNum;
	}
	public int getSrcLineNum() {
		return srcLineNum;
	}
	public void setSrcLineNum(int srcLineNum) {
		this.srcLineNum = srcLineNum;
	}
	public int getTrainNum() {
		return trainNum;
	}
	public void setTrainNum(int trainNum) {
		this.trainNum = trainNum;
	}
	public int getDstLineNum() {
		return dstLineNum;
	}
	public void setDstLineNum(int dstLineNum) {
		this.dstLineNum = dstLineNum;
	}
	public String getDstCode() {
		return dstCode;
	}
	public void setDstCode(String dstCode) {
		this.dstCode = dstCode;
	}
	public short getTrainDir() {
		return trainDir;
	}
	public void setTrainDir(short trainDir) {
		this.trainDir = trainDir;
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
	public short getStopStatus() {
		return stopStatus;
	}
	public void setStopStatus(short stopStatus) {
		this.stopStatus = stopStatus;
	}
	public String getTrainHeaderAtLogic() {
		return trainHeaderAtLogic;
	}
	public void setTrainHeaderAtLogic(String trainHeaderAtLogic) {
		this.trainHeaderAtLogic = trainHeaderAtLogic;
	}
	public String getTrainHeaderAtphysical() {
		return trainHeaderAtphysical;
	}
	public void setTrainHeaderAtphysical(String trainHeaderAtphysical) {
		this.trainHeaderAtphysical = trainHeaderAtphysical;
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
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
