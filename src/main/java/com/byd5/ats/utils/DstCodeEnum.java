package com.byd5.ats.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令编号与描述枚举类
 * @author wu.xianglan
 *
 */
public enum DstCodeEnum {
	type9("ZF",9),
	type7("AB",7),
	type8("AC",8),
	type1("AD",1),
	type2("AE",2),
	type3("AF",3),
	type4("AG",4),
	type5("AH",5),
	type6("AI",6),
	type0("ZH",0);
	
	private String dstCode;
	private Integer platformId;
	

	private static final Map<String, DstCodeEnum> CODE_MAP = new HashMap<String, DstCodeEnum>();
	static {
		for (DstCodeEnum dstCodeEnum : DstCodeEnum.values()) {
			CODE_MAP.put(dstCodeEnum.getDstCode(), dstCodeEnum);
		}
	}

	DstCodeEnum(String dstCode, Integer platformId){
		this.platformId=platformId;
		this.dstCode=dstCode;
	}

	public Integer getPlatformId() {
		return platformId;
	}
	public String getDstCode() {
		return dstCode;
	}


	public String toString(){
		return ""+this.dstCode;
	}

/*	public static AlarmLevel getByCode(Integer code) {
		for (AlarmLevel alarmLevel : values()) {
			if (alarmLevel.getCode().equals(code)) {
				return alarmLevel;
			}
		}
		return null;
	}*/
	public static DstCodeEnum getByDstCode(String dstCode) {
		return CODE_MAP.get(dstCode);
	}
	
	public static void main(String[] args){
		System.out.println(DstCodeEnum.getByDstCode("ZF").getPlatformId());
	}
}

