package com.byd5.ats.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 报警信息类
 * @author wu.xianglan
 *
 */
public class ATSAlarmEvent {

	private int alarmCode;//告警编码
	private String timeString;//时间
	private Long deviceId;//设备ID
	private String deviceInfo;//设备信息
	private String location;//报警地点

	/*public ATSAlarmEvent(int alarmCode, String timeString, Long deviceId, String deviceInfo, String location) {
		this.alarmCode = alarmCode;
		this.timeString = timeString;
		this.deviceId = deviceId;
		this.deviceInfo = deviceInfo;
		this.location = location;
	}*/
	
	public ATSAlarmEvent(String deviceInfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.alarmCode = 5203;
		this.timeString = sdf.format(new Date());
		this.deviceId = (long) 39;
		this.deviceInfo = deviceInfo;
		this.location = "39";
	}

	public int getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(int alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getTimeString() {
		return timeString;
	}

	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String toString(){
		ObjectMapper mapper = new ObjectMapper(); // 转换器
		String str = null;
		try {
			str = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return str;
	}
	
}
