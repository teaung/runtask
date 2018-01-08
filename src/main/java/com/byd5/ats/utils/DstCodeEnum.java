package com.byd5.ats.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令编号与描述枚举类
 * @author wu.xianglan
 *
 */
public enum DstCodeEnum {

	ZH("转换轨", "0x0C401704", 0),//0x0C400710
	ZF("折返轨", "0x0C400702", 9),
	AB("站台七", "0x0C400706", 1),
	AC("站台八", "0x0C400404", 2),
	AD("站台一", "0x0C400104", 3),
	AE("站台二", "0x0C400204", 4),
	AF("站台三", "0x0C400304", 5),
	AG("站台四", "0x0C400404", 6),
	AH("站台五", "0x0C400504", 7),
	AI("站台六", "0x0C400604", 8);
	
	//成员变量
	private String chineseName;
	private String physicalPt;//所在物理区段
	private int platformId;
	
	public String getChineseName() {
		return chineseName;
	}
	public void setChineseName(String chineseName) {
		this.chineseName = chineseName;
	}
	public String getPhysicalPt() {
		return physicalPt;
	}
	public void setPhysicalPt(String physicalPt) {
		this.physicalPt = physicalPt;
	}
	public int getPlatformId() {
		return platformId;
	}
	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}


	private static final Map<String, DstCodeEnum> CODE_MAP = new HashMap<String, DstCodeEnum>();
	static {
		for (DstCodeEnum dstCodeEnum : DstCodeEnum.values()) {
			CODE_MAP.put(dstCodeEnum.name(), dstCodeEnum);
		}
	}
	/**
	 * 根据目的地号获取站台ID
	 * @param dstCode
	 * @return
	 */
	public static Integer getPlatformIdByDstCode(String dstCode) {
		Integer platformId = 0;
		DstCodeEnum dstCodeEnum = CODE_MAP.get(dstCode);
		if(dstCodeEnum != null){
			platformId = dstCodeEnum.getPlatformId();
		}
		return platformId;
	}

	//构造方法
	private DstCodeEnum(String chineseName, String physicalPt, int platformId) {
		this.chineseName = chineseName;
		this.physicalPt = physicalPt;
		this.platformId = platformId;
	}
	
	//根据物理区段获取站台ID
	public static Integer getPlatformIdByPhysicalPt(String physicalPt) {
		for (DstCodeEnum des : DstCodeEnum.values()) {
			if (des.getPhysicalPt().equals(physicalPt)) {
				return des.getPlatformId();
			} 
		}
		return null;
	}
	
	 /*[0,0,"转换轨","ZH",0,180],
	    [1,9,"T0702","ZF",0,149],
	    [2,1,"车站七","AB",0,149],
	    [3,2,"车站八","AC",30,159],
	    [4,3,"车站一","AD",40,173],
	    [5,4,"车站二","AE",30,153],
	    [6,5,"车站三","AF",30,190],
	    [7,6,"车站四","AG",30,128],
	    [8,7,"车站五","AH",30,159],
	    [9,8,"车站六","AI",30,114]*/

	public static void main(String[] args){
		System.out.println(DstCodeEnum.getPlatformIdByPhysicalPt("0x0C400710"));
		System.out.println(DstCodeEnum.getPlatformIdByDstCode("ZF"));
	}
}

