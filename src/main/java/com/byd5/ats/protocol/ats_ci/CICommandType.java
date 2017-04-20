package com.byd5.ats.protocol.ats_ci;

/*
 * ATS发送的命令类型（ATS->CI）
 * 参考：《LRTSW-SYS-ATS与CI通信接口协议》
 * 非周期发送
 * 通信超时中断时间为6s
 */
/*
// cu_pub.h
// a. 按钮及控制命令信息
typedef struct _ats_msg_command
{
	uint8_t  command_num;               // 控制命令个数
	uint8_t  command_type;              // 控制命令类型 
	uint32_t object_id;                 // 控制命令参数 -> 设备ID
	uint8_t  object_type;               // 控制命令参数 -> 设备类型
	uint8_t  object_other;              // 控制命令参数 -> 设备其他
}ats_msg_command_t;
 */

public class CICommandType {

	/*
	 * 1. 办理进路： 0x01 + 进路ID
	 */
	public static final byte CMDTYPE_SET_ROUTE = 0x01;
	
	/*
	 * 2. 设置保护区段： 0x02 + 保护区段ID
	 */
	public static final byte CMDTYPE_SET_PROTECT_SECTION = 0x02;

	/*
	 * 3. 总取消： 0x03 + 进路ID
	 */
	public static final byte CMDTYPE_ALL_CANCEL = 0x03;
	
	/*
	 * 4. 道岔总定操作： 0x04 + 道岔ID
	 */
	public static final byte CMDTYPE_SWITCH_ALL_FIXED = 0x04;

	
/*
# 附录C ATS发送命令类型及参数信息定义
|序号|命令类型|说明|命令参数说明|
|---|---|---|---|
| 1 | 0x01 | 办理进路 | 进路ID |	
| 2 | 0x02 | 设置保护区段	| 保护区段ID |
| 3 | 0x03 | 总取消 | 进路ID |
| 4 | 0x04 | 道岔总定操作 | 道岔ID |
| 5 | 0x05 | 道岔总反操作 | 道岔ID |
| 6 | 0x06 | 道岔单锁操作 | 道岔ID |
| 7 | 0x07 | 道岔解锁操作 | 道岔ID |
| 8 | 0x08 | 道岔封锁操作 | 道岔ID |
| 9 | 0x09 | 道岔解封操作 | 道岔ID |
| 10 | 0x10 | 强扳道岔操作 | 道岔ID +0xaa(预处理)/0x55(执行) |
| 11 | 0x11 | 进路人解 | 进路ID |
| 12 | 0x12 | 区段故障解锁 | 区段ID |
| 13 | 0x13 | 引导总锁 | 无 |
| 14 | 0x14 | 区间闭塞 | 区段ID |
| 15 | 0x15 | 辅助功能 | 语音暂停按钮ID + 0xaa(按下)/0x55(抬起) | 
| 16 | 0x16 | 按钮封闭 | 按钮ID |
| 17 | 0x17 | 按钮解封 | 按钮ID |
| 18 | 0x18 | 引导进路办理 | 进路ID |
| 19 | 0x19 | 取消引导进路 | 进路ID |
| 20 | 0x20 | 区段封锁操作 | 区段ID |
| 21 | 0x21 | 区段解封操作 | 区段ID |
| 22 | 0x22 | 计轴复位 | 区段ID +0xaa(预处理)/0x55(执行)|
| 23 | 0x23 | 设置联锁自动触发进路 | 进路ID |
| 24 | 0x24 | 取消联锁自动触发进路 | 进路ID |
| 25 | 0x25 | 设置自动折返进路 | 进路ID | 
| 26 | 0x26 | 取消自动折返进路 | 进路ID | 
| 27 | 0x27 | 重开信号 | 信号机ID |
| 28 | 0x28 | 信号机封锁 | 信号机ID |
| 29 | 0x29 | 信号机解封 | 信号机ID |
| 30 | 0x30 | 信号关闭 | 信号机ID|
| 31 | 0x31 | 中心扣车| 站台ID |
| 32 | 0x32 | 取消中心扣车| 站台ID |
| 33 | 0x33 | 车站扣车| 站台ID |
| 34 | 0x34 | 取消车站扣车| 站台ID |
| 35 | 0x35 | SPKS按钮 |SPKS按钮 |
| 36 | 0x36 | 设置联锁自动通过进路 | 进路ID |
| 37 | 0x37 | 取消联锁自动通过进路 | 进路ID |
 */
	
}
