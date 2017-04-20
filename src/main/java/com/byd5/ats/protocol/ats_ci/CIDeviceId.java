package com.byd5.ats.protocol.ats_ci;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * CI状态信息帧中的设备Id（CU->ATS）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 
 * CU收到CI状态信息帧后，添加CI所有的设备Id，并发送给ATS。
 */
/*
// cu_pub.h
// id信息
typedef struct _device_id
{
	uint16_t s_id[SIGNAL_NUM];          // 信号机id
	uint16_t sw_id[SWITCH_NUM];         // 道岔id
	uint16_t t_id[PHY_TRACK_NUM];       // 物理区段id
	uint16_t lt_id[LOGIC_TRACK_NUM];    // 逻辑区段id
	uint16_t r_id[ROUTE_NUM];           // 进路id
	uint16_t autopass_id[AUTOPASS_NUM]; // 自动通过id
	uint16_t d_id[DOOR_NUM];            // 站台门id
	uint16_t esp_id[ESP_NUM];           // 紧急关闭按钮id
	uint16_t keep_train_id[KEEP_TRAIN_NUM];         // 扣车id
	uint16_t autoback_id[AUTOBACK_NUM];             // 自动折返id
	uint16_t autoback_fully_id[AUTOBACK_FULLY_NUM]; //全自动折返id
	uint16_t spks_id[SPKS_NUM];         // spks按钮id
	uint16_t autotrig_id[AUTOTRIG_NUM]; // 自动触发id
}dev_id_t;
 */
public class CIDeviceId {

	@JsonProperty("s_id")
	public List<Short> signalId = new ArrayList<Short>();       // 信号机id
	
	@JsonProperty("sw_id")
	public List<Short> switchId = new ArrayList<Short>();      // 道岔id

	@JsonProperty("t_id")
	public List<Short> trackId = new ArrayList<Short>();	// 物理区段id

	@JsonProperty("lt_id")
	public List<Integer> logicId = new ArrayList<Integer>();	// 逻辑区段id

	@JsonProperty("r_id")
	public List<Short> routeId = new ArrayList<Short>();	// 进路id

	@JsonProperty("autopass_id")
	public List<Short> autopassId = new ArrayList<Short>();	// 自动通过id

	@JsonProperty("d_id")
	public List<Integer> platformDoorId = new ArrayList<Integer>();	// 站台门id

	@JsonProperty("esp_id")
	public List<Short> espId = new ArrayList<Short>();	// 紧急关闭按钮id

	@JsonProperty("keep_train_id")
	public List<Short> detainId = new ArrayList<Short>();	// 扣车id

	@JsonProperty("autoback_id")
	public List<Short> autoReturnId = new ArrayList<Short>();	// 自动折返id
	
	@JsonProperty("autoback_fully_id") 
	public List<Short> allAutoReturnId = new ArrayList<Short>();	// 全自动折返id
	
	@JsonProperty("spks_id")
	public List<Short> spksId = new ArrayList<Short>();	// spks按钮id
	
	@JsonProperty("autotrig_id")
	public List<Short> ciATRId = new ArrayList<Short>();	// 自动触发id
	
}
