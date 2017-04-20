package com.byd5.ats.protocol.ats_ci;

/*
 * ATS发送的命令中的设备类型（ATS->CI）
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
public class CIDeviceType {

	/*
	 * 1. 道岔： 0x01
	 */
	public static final byte DEVICETYPE_SWITCH = 0x01;
	
	/*
	 * 2. 信号机： 0x02
	 */
	public static final byte DEVICETYPE_SIGNAL = 0x02;

	/*
	 * 3. 物理区段： 0x03
	 */
	public static final byte DEVICETYPE_PHYSICAL_SECTION = 0x03;
	
	/*
	 * 4. 逻辑区段： 0x04
	 */
	public static final byte DEVICETYPE_LOGICAL_SECTION = 0x04;

	/*
	 * 5. 保护区段： 0x05
	 */
	public static final byte DEVICETYPE_PROTECT_SECTION = 0x05;

	/*
	 * 6. 进路： 0x06
	 */
	public static final byte DEVICETYPE_ROUTE = 0x06;
	
	/*
	 * 7. 站台： 0x07
	 */
	public static final byte DEVICETYPE_PLATFORM = 0x07;
	
	/*
	 * 8. 计轴区段： 0x08
	 */
	public static final byte DEVICETYPE_AXLE_SECTION = 0x08;
	
	/*
	 * 9. 自动折返： 0x09
	 */
	public static final byte DEVICETYPE_AUTO_RETURN = 0x09;
	
	/*
	 * 10. 自动通过： 0x0A
	 */
	public static final byte DEVICETYPE_AUTO_PASS = 0x0A;
	
	/*
	 * 11. SPKS按钮： 0x0B
	 */
	public static final byte DEVICETYPE_SPKS_BUTTON = 0x0B;
}
