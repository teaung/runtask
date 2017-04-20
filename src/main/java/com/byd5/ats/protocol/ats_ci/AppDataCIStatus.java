package com.byd5.ats.protocol.ats_ci;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 站场状态信息帧（CI->ATS）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 400ms周期发送
 * 通信超时中断时间为6s
 */
/*
// cu_pub.h
#define SIGNAL_NUM      14  // 信号机数量
#define SWITCH_NUM      1   //  道岔数量 
#define PHY_TRACK_NUM   32  // 物理区段数量 
#define LOGIC_TRACK_NUM 93  // 逻辑区段数量
#define ROUTE_NUM       13  // 进路数量 
#define AUTOPASS_NUM    1   // 自动通过进路数量 
#define DOOR_NUM        12  // 站台门数量 
#define ESP_NUM         1   // 紧急关闭数量 
#define KEEP_TRAIN_NUM  1   // 扣车数量 
#define AUTOBACK_NUM    1   // 自动折返数量 
#define AUTOBACK_FULLY_NUM  1 // 全自动折返数量 
#define SPKS_NUM        1   // SPKS数量 
#define AUTOTRIG_NUM    1   // 自动触发数量 
#define TRAIN_NUM       8   // 列车数量 
#define CI_NUM          3   // CI数量 
#define ZC_NUM          3   // ZC数量 
// a. 站场状态信息帧
typedef struct _ci_msg_status
{
	uint8_t s_status[SIGNAL_NUM];       // 信号机状态 
	uint8_t sw_status[SWITCH_NUM];      // 道岔状态 
	uint8_t t_status[PHY_TRACK_NUM];    // 物理区段状态 
	uint8_t lt_status[LOGIC_TRACK_NUM]; // 逻辑区段状态 
	uint8_t r_status[ROUTE_NUM];        // 进路状态 
	uint8_t autopass_status[AUTOPASS_NUM];  // 自动通过状态 
	uint8_t d_status[DOOR_NUM];             // 站台门状态 
	uint8_t esp_status[ESP_NUM];            // 紧急关闭状态 
	uint8_t keep_train[KEEP_TRAIN_NUM];     // 扣车状态 
	uint8_t autoback_status[AUTOBACK_NUM];  // 自动折返状态 
	uint8_t autoback_fully_status[AUTOBACK_FULLY_NUM]; // 全自动折返状态 
	uint8_t spks_status[SPKS_NUM];          // spks按钮状态 
	uint8_t autotrig_status[AUTOTRIG_NUM];  // 自动触发状态 
}ci_msg_status_t;
 */
public class AppDataCIStatus {

	/*
	 * 消息类型（2字节）：0x0202=站场状态信息（CI->ATS）
	 */
	//private short type;
	
	/*
	 * 信号机状态（n=14字节）：每一个字节表示一个信号机的状态
	 * 1. 信号机相关码位信息（共8bit）
	 * 1）信号机颜色信息：共红、黄、绿、红黄、白5个颜色，占用5bit；1=显示；0=不显示。
	 * 2）信号机亮灭信息：占用1bit；1=信号机灭灯；0=信号机亮灯；
	 * 3）信号机灯丝断丝状态：占用1bit；1=信号机灯位未断丝；0=信号机灯位断丝；
	 * 4）信号机封锁状态：占用1bit；1=信号机未封锁；0=信号机封锁；
	 */
	//private List<Byte> s_status;       // 信号机状态 
	@JsonProperty("s_status")
	/*private List<Byte> s_status = new ArrayList<Byte>();       // 信号机状态 */
	/*private List<Byte> signalStatus = new ArrayList<Byte>();       // 信号机状态 */
	public List<Short> signalStatus = new ArrayList<Short>();       // 信号机状态
	
	/*
	 * 道岔状态（n=1字节）：每一个字节表示一个道岔的状态
	 * 2. 道岔相关码位信息（共7bit）
	 * 1）道岔位置信息：包括道岔定位、道岔反位、道岔挤岔、道岔四开共4种位置状态；占用4bit；1=是；0=否。
	 * 2）道岔单锁状态：占用1bit；1=道岔未单锁；0=道岔单锁；
	 * 3）道岔封锁状态：占用1bit；1=道岔未封锁；0=道岔封锁；
	 * 4）道岔引导总锁状态：占用1bit；1=道岔未引导总锁闭；0=道岔引导总锁闭；
	 */
	@JsonProperty("sw_status")
	public List<Byte> switchStatus = new ArrayList<Byte>();      // 道岔状态
	
	/*
	 * 物理区段状态（n=32字节）：每一个字节表示一个物理区段的状态
	 * 3. 物理区段状态相关码位信息（共7bit）
	 * 1）物理区段锁闭状态：分为上电锁闭、进路锁闭、防护锁闭、故障锁闭共4种锁闭状态；占用4bit；1=区段处于该锁闭状态；0=区段未处于该锁闭状态。
	 * 2）物理区段占用状态：占用1bit；1=未占用；0=占用；
	 * 3）物理区段ARB状态：占用1bit；1=非ARB；0=ARB；
	 * 4）物理区段封锁状态：占用1bit；1=未封锁；0=封锁；
	 */
	@JsonProperty("t_status")
	public List<Byte> trackStatus = new ArrayList<Byte>();	// 物理区段状态 
	
	/*
	 * 逻辑区段状态（n=93字节）：每一个字节表示一个逻辑区段的状态
	 * 4. 逻辑区段状态相关码位信息（共2bit）
	 * 含通信车占用、非通信车占用、空闲共3种状态，即01、10、11；00的情况不存在。
	 * 1）通信车占用状态：占用1bit；1=未被通信车占用；0=被通信车占用；
	 * 2）非通信车占用状态：占用1bit；1=未被非通信车占用；0=被非通信车占用；
	 */
	@JsonProperty("lt_status")
	public List<Byte> logicStatus = new ArrayList<Byte>();	// 逻辑区段状态
	
	/*
	 * 进路状态（n=13字节）：每一个字节表示一个进路的状态
	 * 5. 进路状态相关码位信息（共3bit）
	 * 进路状态包括空闲、锁闭、选排、延时解锁等4个状态，共占用3bit。
	 * 1）选排：占用1bit；1=进路处于选排状态；0=进路未处于选排状态；
	 * 2）延时解锁：占用1bit；1=进路处于延时解锁状态；0=进路未处于延时解锁状态；
	 * 3）锁闭：占用1bit；1=进路处于锁闭状态；0=进路未处于锁闭状态；
	 */
	@JsonProperty("r_status")
	public List<Byte> routeStatus = new ArrayList<Byte>();	// 进路状态 
	
	/*
	 * 联锁自动通过进路状态（n=1字节）：
	 * 6. 联锁自动通过进路状态相关码位信息（共1bit）
	 * 联锁自动通过进路状态包括设置、未设置共2种状态，共占用1bit。
	 * 1）联锁自动通过进路状态：占用1bit；1=设置；0=未设置；
	 */
	@JsonProperty("autopass_status")
	public List<Byte> autopassStatus = new ArrayList<Byte>();	// 自动通过状态
	
	/*
	 * 站台屏蔽门状态（n=12字节）：每一个字节表示一个站台屏蔽门的状态
	 * 7. 站台屏蔽门状态相关码位信息（共2bit）
	 * 站台屏蔽门状态包括打开、关闭、互锁解除共3种状态，共占用2bit。
	 * 1）互锁解除状态：占用1bit；1=互锁解除；0=未互锁解除；
	 * 2）站台屏蔽门开关状态：占用1bit；1=关闭；0=打开；
	 */
	@JsonProperty("d_status")
	public List<Byte> platformDoorStatus = new ArrayList<Byte>();	// 站台门状态
	
	/*
	 * 紧急关闭按钮状态（n=1字节）：
	 * 8. 紧急关闭按钮状态相关码位信息（共1bit）
	 * 紧急关闭按钮状态包括按下、未按下共2种状态，共占用1bit。
	 * 1）紧急关闭按钮状态：占用1bit；1=未按下；0=按下；
	 */
	@JsonProperty("esp_status")
	public List<Byte> espStatus = new ArrayList<Byte>();	// 紧急关闭状态 
	
	/*
	 * 信号机对应站台扣车状态（n=1字节）：
	 * 9. 信号机对应站台扣车状态相关码位信息（共2bit）
	 * 扣车状态包括中心扣车/未扣车、车站扣车/未扣车共4种状态，共占用2bit。
	 * 1）车站扣车状态：占用1bit；1=未车站扣车；0=车站扣车；
	 * 2）中心扣车状态：占用1bit；1=未中心扣车；0=中心扣车；
	 */
	@JsonProperty("keep_train")
	public List<Byte> detainStatus = new ArrayList<Byte>();	// 扣车状态
	
	/*
	 * 自动折返状态（n=1字节）：
	 * 10. 自动折返状态相关码位信息（共1bit）
	 * 自动折返状态包括设置、未设置共2种状态，共占用1bit。
	 * 1）自动折返状态：占用1bit；1=未设置；0=设置；
	 */
	@JsonProperty("autoback_status")
	public List<Byte> autoReturnStatus = new ArrayList<Byte>();	// 自动折返状态 
	
	/*
	 * 全自动折返状态（n=1字节）：
	 * 11. 全自动折返状态相关码位信息（共1bit）
	 * 全自动折返状态包括设置、未设置共2种状态，共占用1bit。
	 * 1）全自动折返状态：占用1bit；1=未设置；0=设置；
	 */
	@JsonProperty("autoback_fully_status") 
	public List<Byte> allAutoReturnStatus = new ArrayList<Byte>();	// 全自动折返状态 
	
	/*
	 * SPKS按钮状态（n=1字节）：
	 * 12. SPKS按钮状态相关码位信息（共1bit）
	 * SPKS按钮状态包括打开、关闭共2种状态，共占用1bit。
	 * 1）SPKS按钮状态：占用1bit；1=关闭；0=打开；
	 */
	@JsonProperty("spks_status")
	public List<Byte> spksStatus = new ArrayList<Byte>();	// spks按钮状态 
	
	/*
	 * 联锁自动触发进路状态（n=1字节）：
	 * 13. 联锁自动触发进路状态相关码位信息（共1bit）
	 * 联锁自动触发进路状态包括设置、未设置共2种状态，共占用1bit。
	 * 1）联锁自动触发进路状态：占用1bit；1=未设置；0=设置；
	 */
	@JsonProperty("autotrig_status")
	public List<Byte> ciATRStatus = new ArrayList<Byte>();	// 自动触发状态 
	
	
/*	public List<Byte> getSignalStatus() {
		return signalStatus;
	}
	public void setSignalStatus(List<Byte> signalStatus) {
		this.signalStatus = signalStatus;
	}*/
	/*public List<Byte> getS_status() {
		return s_status;
	}
	public void setS_status(List<Byte> s_status) {
		this.s_status = s_status;
	}*/
	
/*	public List<Byte> getSw_status() {
		return sw_status;
	}
	public void setSw_status(List<Byte> sw_status) {
		this.sw_status = sw_status;
	}*/
/*	
	public List<Byte> getT_status() {
		return t_status;
	}
	public void setT_status(List<Byte> t_status) {
		this.t_status = t_status;
	}
	public List<Byte> getLt_status() {
		return lt_status;
	}
	public void setLt_status(List<Byte> lt_status) {
		this.lt_status = lt_status;
	}
	
	public List<Byte> getR_status() {
		return r_status;
	}
	public void setR_status(List<Byte> r_status) {
		this.r_status = r_status;
	}

	public List<Byte> getAutopass_status() {
		return autopass_status;
	}
	public void setAutopass_status(List<Byte> autopass_status) {
		this.autopass_status = autopass_status;
	}
	
	public List<Byte> getD_status() {
		return d_status;
	}
	public void setD_status(List<Byte> d_status) {
		this.d_status = d_status;
	}
	
	public List<Byte> getEsp_status() {
		return esp_status;
	}
	public void setEsp_status(List<Byte> esp_status) {
		this.esp_status = esp_status;
	}
	
	public List<Byte> getKeep_train() {
		return keep_train;
	}
	public void setKeep_train(List<Byte> keep_train) {
		this.keep_train = keep_train;
	}
	
	public List<Byte> getAutoback_status() {
		return autoback_status;
	}
	public void setAutoback_status(List<Byte> autoback_status) {
		this.autoback_status = autoback_status;
	}
	
	public List<Byte> getAutoback_fully_status() {
		return autoback_fully_status;
	}
	public void setAutoback_fully_status(List<Byte> autoback_fully_status) {
		this.autoback_fully_status = autoback_fully_status;
	}
	
	public List<Byte> getSpks_status() {
		return spks_status;
	}
	public void setSpks_status(List<Byte> spks_status) {
		this.spks_status = spks_status;
	}
	
	public List<Byte> getAutotrig_status() {
		return autotrig_status;
	}
	public void setAutotrig_status(List<Byte> autotrig_status) {
		this.autotrig_status = autotrig_status;
	}*/
}
