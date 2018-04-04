package com.byd5.ats.message;

//import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 3.2 应用数据层：ATO命令信息帧（非周期）（ATS->VOBC）
 * 参考：《LRTSW-SYS-VOBC与ATS通信接口协议》
 * 当ATS设备所管辖范围内有被控列车，ATS应周期向该VOBC发送ATO命令信息。

|字段序号|接口内容|字节长度|取值|备注|
|:---:|---|:---:|---|---|
|1|消息类型|2|0x0203|ATO命令信息|
|2|服务号/表号|2|00~65534有效<br>默认值：0xFFFF|列车为非计划车时，发送默认值|
|3|线路编号|2|线路编号|全网统一标识|
|4|下一ZC ID[注1]|4|VOBC最大安全前端所在ZC管辖区域的下一个ZC ID<br>默认值为0xFFFFFFFF|“最大安全前端”的定义在具体工程项目中明确|
|5|下一CI ID[注1]|4|VOBC最大安全前端所在CI管辖区域的下一个CI ID<br>默认值为0xFFFFFFFF||
|6|下一ATS ID[注1]|4|VOBC最大安全前端所在ATS管辖区域的下一个ATS ID<br>默认值为0xFFFFFFFF||
|7|车组所属线路编号|2|车组所属线路编号|全网统一标识|
|8|车组号|2|001~999|“车组所属线路号”+“车组号”在全线网内为唯一标识|
|9|源线路号|2|列车始发站线路编号<br>默认值:0xFFFF|全网统一标识|
|10|车次号|2|0001~9999|默认值0000|
|11|目的地线路号|2|目的地线路编号，同线路编号<br>默认值：0xFFFF|列车为非计划车时，发送默认值|
|12|目的地号|4|默认值：0xFFFFFFFF|用ASCII码标识，最多4个ASCII码，低于4个高位用空格补齐<br>列车为非计划车时，发送默认值<br>具体定义方式在工程中明确|
|13|计划运行方向|1|上行：0x55<br>下行：0xAA<br>其他：0xFF||
|14|跳停站台ID[注2]|2|下一站跳停：站台ID<br>下一站不跳停：0xFFFF||
|15|下一停车站台ID|2|默认值：0xFFFF|定义同“跳停站台ID”|
|16|站停时间|2|立即发车：0x0001<br>站停时间：大于0x0002，单位秒<br>无效值：0xFFFF|其它非法|
|17|下一站跳停命令|1|下一站跳停：0x55<br>下一站无/取消跳停：0xAA<br>无效值：0xFF|其它非法|
|18|区间运行调整命令|2||区间等级或区间运行时间<br>根据业主需求确定|
|19|扣车命令[注3]|1|扣车有效：0x55<br>扣车取消/无扣车：0xAA<br>无效值：0xFF|其它非法|
|20|折返命令[注4]|1|站前折返：0x55<br>有人站后折返：0xCC<br>无人自动折返：0xAA<br>不折返：0xFF||
|21|回段指示|1|回段：0x55<br>不回段：0xAA<br>默认值：0xFF[注5]||
|22|门控策略|1|开左门：0x55<br>开右门：0xCC<br>同时开双侧门：0xAA<br>先开左门再开右门：0x11<br>先开右门再开左门：0x22<br>默认值：0xFF|开门间隔在具体工程项目中确认<br>站台为双侧门时，发送门控策略<br>单侧门时，发送默认值|
|23|预留|4||||

注1:
> 下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。

注2：

1. 有跳停命令时，ATS判断列车最大安全前端在跳停站台的前一站台或折返轨内时，下发后一站台的跳停命令；
1. 下发跳停命令时，“跳停站台ID”内容应为列车最大安全前端所在区段前方（不含此区段）最近的跳停站台ID，“下一停车站台ID”内容应为此跳停命令对应的跳停站台后，IE车第一个要停车的站台ID；
1. 无跳停命令时，“跳停站台ID”内容为默认值；“下一停车站台ID”内容为列车最大安全前端所在区段前方（不含此区段）最近的站台ID。

注3：
> 列车最大安全前端所在站台有扣车命令时，ATS发送“扣车有效”；否则，发送“扣车取消，无扣车”。

注4：

1. 对于计划列车，列车最大安全前端在站台内时，ATS按照列车运行计划判断列车是否要进行折返：
    - 若列车要进行站前折返，则ATS发送站前折返命令，VOBC收到后，列车在站台停稳后，显示换端提示；
    - 若列车要进行有人站后折返，则ATS发送有人站后折返命令，VOBC收到后，在运行至此站台后的站后折返轨停车后，显示换端提示；
    - 若列车要进行无人自动折返，则ATS发送无人自动折返命令，VOBC收到后，列车在站台停稳后，显示无人自动折返提示；
    - 若列车不进行折返，则ATS发送不折返命令，VOBC收到后，不显示折返提示；
2. 对于非计划车，ATS发送“不折返”命令；
3. 对于非计划列车或与ATS无通讯的列车，VOBC根据车载电子地图存储区段属性显示折返提示。

注5：

1. 当列车最大安全前端不在转换轨内或列车不为回段方向时， ATS向VOBC发送的“回段指示”字段为默认值；
1. 当列车最大安全前端在转换轨内且列车为回段方向时，对于计划列车，ATS根据列车运行计划，向VOBC发送“回段”或“不回段”提示，对于非计划列车，ATS向VOBC发送默认值；
1. VOBC与ATS通信正常且收到的回段提示字段非默认值时，根据ATS回段提示信息，判断在转换轨内是否显示回段提示；VOBC与ATS通信断开或收到的回段提示字段为默认值时，根据电子地图配置的区段属性，在转换轨内显示回段提示。

typedef struct _app_data__ats2vobc_ato_command
{
	// 0. 信息长度：报文长度（信息类型-信息结束） //
	uint16_t msg_len;
	// 1. 信息类型：定义某一条应用信息的标识
		信息类型编号：0x0203
	//
	uint16_t msg_type;
	
	// 2. 服务号/表号（2字节）
		00~65534有效
		默认值：0xFFFF
		列车为非计划车时，发送默认值
	//
	uint16_t service_num;

	// 3. 线路编号（2字节）
		线路编号, 全网统一标识
	//
	uint16_t line_num;
	
	// 4. 下一ZC ID（4字节）
		VOBC最大安全前端所在ZC管辖区域的下一个ZC ID
		默认值为0xFFFFFFFF
		“最大安全前端”的定义在具体工程项目中明确
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。
	//
	uint32_t next_zc_id;
	
	// 5. 下一CI ID（4字节）
		VOBC最大安全前端所在CI管辖区域的下一个CI ID
		默认值为0xFFFFFFFF
		“最大安全前端”的定义在具体工程项目中明确
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。
	//
	uint32_t next_ci_id;
	
	// 6. 下一ATS ID（4字节）
		VOBC最大安全前端所在ATS管辖区域的下一个ATS ID
		默认值为0xFFFFFFFF
		“最大安全前端”的定义在具体工程项目中明确
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。
	//
	uint32_t next_ats_id;
	
	// 7. 车组所属线路编号（2字节）
		车组所属线路编号, 全网统一标识
	//
	uint16_t cargroup_line_num;
	
	// 8. 车组号（2字节）
		001~999有效，超出范围无效
		“车组所属线路号”+“车组号”在全线网内为唯一标识
	//
	uint16_t cargroup_num;
	
	// 9. 源线路号（2字节）
		列车始发站线路编号，全网统一标识
		默认值:0xFFFF
	//
	uint16_t src_line_num;
	
	// 10. 车次号（2字节）
		0001~9999有效，超出范围无效，默认值：0000。
	//
	uint16_t train_num;
	
	// 11. 目的线路号（2字节）
		目的地线路编号，同线路编号
		默认值:0xFFFF
		列车为非计划车时，发送默认值
	//
	uint16_t dst_line_num;
	
	// 12. 目的地号（4字节）
		列车运行计划中列车运行的终点。用ASCII码表示，最多4个ASCII码，低于4个高位用空格补齐。
		默认值：0xFFFFFFFF
		列车为非计划车时，发送默认值
		具体定义方式在工程中明确
	//
	uint32_t dst_code;
	
	// 13. 计划运行方向（1字节）
		上行：0x55
		下行：0xAA
		其他：0xFF
	//
	uint8_t plan_dir;
	
	// 14. 跳停站台ID（2字节）
		下一站跳停时，取值为站台ID。下一站不跳停时取值为0xFFFF
注2：
a. 有跳停命令时，ATS判断列车最大安全前端在跳停站台的前一站台或折返轨内时，下发后一站台的跳停命令；
b. 下发跳停命令时，“跳停站台ID”内容应为列车最大安全前端所在区段前方（不含此区段）最近的跳停站台ID，“下一停车站台ID”内容应为此跳停命令对应的跳停站台后，IE车第一个要停车的站台ID；
c. 无跳停命令时，“跳停站台ID”内容为默认值；“下一停车站台ID”内容为列车最大安全前端所在区段前方（不含此区段）最近的站台ID。
	//
	uint16_t skip_platform_id;
	
	// 15. 下一停车站台ID（2字节）
		前方最近的停车站台ID，定义同“跳停站台ID”，默认值为0xFFFF
		同上：注2
	//
	uint16_t next_stop_platform_id;
	
	// 16. 站停时间（2字节）
		立即发车：0x0001
		站停时间：大于0x0002，单位：秒
		无效值：0xFFFF
		其它非法
	//
	uint16_t platform_stop_time;
	
	// 17. 下一站跳停命令（1字节）
		下一站跳停：0x55
		下一站无跳停命令/取消下一站跳停：0xAA
		无效值：0xFF
		其它非法
	//
	uint8_t next_skip_cmd;

	// 18. 区间运行调整命令（2字节）
		区间等级或区间运行时间（发车到下一站停车），根据业主需求确定
	//
	uint16_t section_run_adjust_cmd;

	// 19. 扣车命令（1字节）
		扣车有效：0x55
		扣车取消，无扣车：0xAA
		无效值：0xFF
		其它非法
注3：列车最大安全前端所在站台有扣车命令时，ATS发送“扣车有效”；否则，发送“扣车取消，无扣车”。
	//
	uint8_t detain_cmd;

	// 20. 折返命令（1字节）
		是否进行折返
		站前折返：0x55
		有人站后折返：0xCC
		无人自动折返：0xAA
		不折返：0xFF
注4：
1. 对于计划列车，列车最大安全前端在站台内时，ATS按照列车运行计划判断列车是否要进行折返：
    - 若列车要进行站前折返，则ATS发送站前折返命令，VOBC收到后，列车在站台停稳后，显示换端提示；
    - 若列车要进行有人站后折返，则ATS发送有人站后折返命令，VOBC收到后，在运行至此站台后的站后折返轨停车后，显示换端提示；
    - 若列车要进行无人自动折返，则ATS发送无人自动折返命令，VOBC收到后，列车在站台停稳后，显示无人自动折返提示；
    - 若列车不进行折返，则ATS发送不折返命令，VOBC收到后，不显示折返提示；
2. 对于非计划车，ATS发送“不折返”命令；
3. 对于非计划列车或与ATS无通讯的列车，VOBC根据车载电子地图存储区段属性显示折返提示。

	//
	uint8_t turnback_cmd;

	// 21. 回段指示（1字节）
		是否进行回段。
		回段：0x55，不回段：0xAA。默认值：0xFF[注5]
注5：
a. 当列车最大安全前端不在转换轨内或列车不为回段方向时， ATS向VOBC发送的“回段指示”字段为默认值；
b. 当列车最大安全前端在转换轨内且列车为回段方向时，对于计划列车，ATS根据列车运行计划，向VOBC发送“回段”或“不回段”提示，对于非计划列车，ATS向VOBC发送默认值；
c. VOBC与ATS通信正常且收到的回段提示字段非默认值时，根据ATS回段提示信息，判断在转换轨内是否显示回段提示；VOBC与ATS通信断开或收到的回段提示字段为默认值时，根据电子地图配置的区段属性，在转换轨内显示回段提示。
	//
	uint8_t back_depot_cmd;

	// 22. 门控策略（1字节）
		开关门策略
		开左门：0x55
		开右门：0xCC
		同时开双侧门：0xAA
		先开左门再开右门：0x11 （开门间隔在具体工程项目中确认）
		先开右门再开左门：0x22
		默认值：0xFF
		站台为双侧门时，发送门控策略，单侧门时发送默认值。
	//
	uint8_t gating_strategy;
	
	// 23. 预留（4字节） //
	uint32_t reserved;
	
} app_data__ats2vobc_ato_command_t;
 */
public class AppDataAVAtoCommand {

	/*
	 * 消息类型（2字节）：0x0203=ATO命令信息（ATS->VOBC）
	 */
	/**
	 * 1、报文长度（2字节）：报文类型至报文结束的字节数
	 */
	//@JsonProperty("msg_len")
	private short length;
	
	/**
	 * 2、报文类型（2字节）：定义某一条应用信息的标识
	 * 信息类型编号：0x0203=ATO命令信息（ATS->VOBC）
	 */
	//@JsonProperty("msg_type")
	private short type;
	
	/** 2. 服务号/表号（2字节）
		00~65534有效
		默认值：0xFFFF
		列车为非计划车时，发送默认值
	*/
	//@JsonProperty("service_num")
	private int serviceNum;
	
	/** 3. 线路编号（2字节）
		线路编号, 全网统一标识
	*/
	//@JsonProperty("line_num")
	private int lineNum;
	
	/** 4. 下一ZC ID（4字节） <br>
		VOBC最大安全前端所在ZC管辖区域的下一个ZC ID <br>
		默认值为0xFFFFFFFF <br>
		“最大安全前端”的定义在具体工程项目中明确 <br>
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端 <br>
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。 <br>
	*/
	//@JsonProperty("next_zc_id")
	private long nextZcId;
	
	/** 5. 下一CI ID（4字节） <br>
		VOBC最大安全前端所在CI管辖区域的下一个CI ID <br>
		默认值为0xFFFFFFFF <br>
		“最大安全前端”的定义在具体工程项目中明确 <br>
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端 <br>
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。 <br>
	*/
	//@JsonProperty("next_ci_id")
	private long nextCiId;
	
	/** 6. 下一ATS ID（4字节） <br>
		VOBC最大安全前端所在ATS管辖区域的下一个ATS ID <br>
		默认值为0xFFFFFFFF <br>
		“最大安全前端”的定义在具体工程项目中明确 <br>
		注1:下一ZC/CI/ATSID信息，对于计划列车，ATS按照列车运行计划查找列车最大安全前端 <br>
		所在ZC/CI/ATS的下一个ZC/CI/ATS，并向VOBC发送；对于非计划列车，ATS发送默认值。 <br>
	*/
	//@JsonProperty("next_ats_id")
	private long nextAtsId;
	
	/** 7. 车组所属线路编号（2字节）
		车组所属线路编号, 全网统一标识
	*/
	//@JsonProperty("cargroup_line_num")
	private int cargroupLineNum;
	
	/** 8. 车组号（2字节）
		001~999有效，超出范围无效
		“车组所属线路号”+“车组号”在全线网内为唯一标识
	*/
	//@JsonProperty("cargroup_num")
	private short cargroupNum;
	
	/** 9. 源线路号（2字节）
		列车始发站线路编号，全网统一标识
		默认值:0xFFFF
	*/
	//@JsonProperty("src_line_num")
	private int srcLineNum;
	
	/** 10. 车次号（2字节）
		0001~9999有效，超出范围无效，默认值：0000。
	*/
	//@JsonProperty("train_num")
	private short trainNum;
	
	/** 11. 目的线路号（2字节）
		目的地线路编号，同线路编号
		默认值:0xFFFF
		列车为非计划车时，发送默认值
	*/
	//@JsonProperty("dst_line_num")
	private int dstLineNum;
	
	/** 12. 目的地号（4字节）
		列车运行计划中列车运行的终点。用ASCII码表示，最多4个ASCII码，低于4个高位用空格补齐。
		默认值：0xFFFFFFFF
		列车为非计划车时，发送默认值
		具体定义方式在工程中明确
	*/
	//@JsonProperty("dst_code")
	private long dstCode;
	
	/** 13. 计划运行方向（1字节）
		上行：0x55
		下行：0xAA
		其他：0xFF
	*/
	//@JsonProperty("plan_dir")
	private short planDir;
	
	/** 14. 跳停站台ID（2字节） <br>
		下一站跳停时，取值为站台ID。下一站不跳停时取值为0xFFFF <br>
	注2： <br>
	a. 有跳停命令时，ATS判断列车最大安全前端在跳停站台的前一站台或折返轨内时，下发后一站台的跳停命令； <br>
	b. 下发跳停命令时，“跳停站台ID”内容应为列车最大安全前端所在区段前方（不含此区段）最近的跳停站台ID，“下一停车站台ID”内容应为此跳停命令对应的跳停站台后，IE车第一个要停车的站台ID； <br>
	c. 无跳停命令时，“跳停站台ID”内容为默认值；“下一停车站台ID”内容为列车最大安全前端所在区段前方（不含此区段）最近的站台ID。 <br>
	*/
	//@JsonProperty("skip_platform_id")
	private int skipPlatformId;
	
	/** 15. 下一停车站台ID（2字节）
		前方最近的停车站台ID，定义同“跳停站台ID”，默认值为0xFFFF
		同上：注2
	*/
	//@JsonProperty("next_stop_platform_id")
	private int nextStopPlatformId;
	
	/** 16. 站停时间（2字节）
		立即发车：0x0001
		站停时间：大于0x0002，单位：秒
		无效值：0xFFFF
		其它非法
	*/
	//@JsonProperty("platform_stop_time")
	private int platformStopTime;
	
	/** 17. 下一站跳停命令（1字节）
		下一站跳停：0x55
		下一站无跳停命令/取消下一站跳停：0xAA
		无效值：0xFF
		其它非法
	*/
	//@JsonProperty("next_skip_cmd")
	private short nextSkipCmd;
	
	/** 18. 区间运行调整命令（2字节）
		区间等级或区间运行时间（发车到下一站停车），根据业主需求确定
	*/
	//@JsonProperty("section_run_adjust_cmd")
	private short sectionRunAdjustCmd;
	
	/** 19. 扣车命令（1字节） <br>
		扣车有效：0x55 <br>
		扣车取消，无扣车：0xAA <br>
		无效值：0xFF <br>
		其它非法 <br>
	注3：列车最大安全前端所在站台有扣车命令时，ATS发送“扣车有效”；否则，发送“扣车取消，无扣车”。 <br>
	*/
	//@JsonProperty("detain_cmd")
	private short detainCmd;
	
	/** 20. 折返命令（1字节） <br>
		是否进行折返 <br>
		站前折返：0x55 <br>
		有人站后折返：0xCC <br>
		无人自动折返：0xAA <br>
		不折返：0xFF <br>
	注4： <br>
	1. 对于计划列车，列车最大安全前端在站台内时，ATS按照列车运行计划判断列车是否要进行折返： <br>
	- 若列车要进行站前折返，则ATS发送站前折返命令，VOBC收到后，列车在站台停稳后，显示换端提示； <br>
	- 若列车要进行有人站后折返，则ATS发送有人站后折返命令，VOBC收到后，在运行至此站台后的站后折返轨停车后，显示换端提示； <br>
	- 若列车要进行无人自动折返，则ATS发送无人自动折返命令，VOBC收到后，列车在站台停稳后，显示无人自动折返提示； <br>
	- 若列车不进行折返，则ATS发送不折返命令，VOBC收到后，不显示折返提示； <br>
	2. 对于非计划车，ATS发送“不折返”命令； <br>
	3. 对于非计划列车或与ATS无通讯的列车，VOBC根据车载电子地图存储区段属性显示折返提示。 <br>
	
	*/
	//@JsonProperty("turnback_cmd")
	private short turnbackCmd;
	
	/** 21. 回段指示（1字节） <br>
		是否进行回段。
		回段：0x55，不回段：0xAA。默认值：0xFF[注5] <br>
	注5： <br>
	a. 当列车最大安全前端不在转换轨内或列车不为回段方向时， ATS向VOBC发送的“回段指示”字段为默认值； <br>
	b. 当列车最大安全前端在转换轨内且列车为回段方向时，对于计划列车，ATS根据列车运行计划，向VOBC发送“回段”或“不回段”提示，对于非计划列车，ATS向VOBC发送默认值； <br>
	c. VOBC与ATS通信正常且收到的回段提示字段非默认值时，根据ATS回段提示信息，判断在转换轨内是否显示回段提示；VOBC与ATS通信断开或收到的回段提示字段为默认值时，根据电子地图配置的区段属性，在转换轨内显示回段提示。
	*/
	//@JsonProperty("back_depot_cmd")
	private short backDepotCmd;
	
	/** 22. 门控策略（1字节） <br>
		开关门策略 <br>
		开左门：0x55 <br>
		开右门：0xCC <br>
		同时开双侧门：0xAA <br>
		先开左门再开右门：0x11 （开门间隔在具体工程项目中确认） <br>
		先开右门再开左门：0x22 <br>
		默认值：0xFF <br>
		站台为双侧门时，发送门控策略，单侧门时发送默认值。 <br>
	*/
	//@JsonProperty("doorctrl_strategy")
	private short doorctrlStrategy;
	
	/** 23. 预留（4字节） */
	//@JsonProperty("reserved")
	private int reserved;

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
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

	public long getNextZcId() {
		return nextZcId;
	}

	public void setNextZcId(long nextZcId) {
		this.nextZcId = nextZcId;
	}

	public long getNextCiId() {
		return nextCiId;
	}

	public void setNextCiId(long nextCiId) {
		this.nextCiId = nextCiId;
	}

	public long getNextAtsId() {
		return nextAtsId;
	}

	public void setNextAtsId(long nextAtsId) {
		this.nextAtsId = nextAtsId;
	}

	public int getCargroupLineNum() {
		return cargroupLineNum;
	}

	public void setCargroupLineNum(int cargroupLineNum) {
		this.cargroupLineNum = cargroupLineNum;
	}

	public short getCargroupNum() {
		return cargroupNum;
	}

	public void setCargroupNum(short cargroupNum) {
		this.cargroupNum = cargroupNum;
	}

	public int getSrcLineNum() {
		return srcLineNum;
	}

	public void setSrcLineNum(int srcLineNum) {
		this.srcLineNum = srcLineNum;
	}

	public short getTrainNum() {
		return trainNum;
	}

	public void setTrainNum(short trainNum) {
		this.trainNum = trainNum;
	}

	public int getDstLineNum() {
		return dstLineNum;
	}

	public void setDstLineNum(int dstLineNum) {
		this.dstLineNum = dstLineNum;
	}

	public long getDstCode() {
		return dstCode;
	}

	public void setDstCode(long dstCode) {
		this.dstCode = dstCode;
	}

	public short getPlanDir() {
		return planDir;
	}

	public void setPlanDir(short planDir) {
		this.planDir = planDir;
	}

	public int getSkipPlatformId() {
		return skipPlatformId;
	}

	public void setSkipPlatformId(int skipPlatformId) {
		this.skipPlatformId = skipPlatformId;
	}

	public int getNextStopPlatformId() {
		return nextStopPlatformId;
	}

	public void setNextStopPlatformId(int nextStopPlatformId) {
		this.nextStopPlatformId = nextStopPlatformId;
	}

	public int getPlatformStopTime() {
		return platformStopTime;
	}

	public void setPlatformStopTime(int platformStopTime) {
		this.platformStopTime = platformStopTime;
	}

	public short getNextSkipCmd() {
		return nextSkipCmd;
	}

	public void setNextSkipCmd(short nextSkipCmd) {
		this.nextSkipCmd = nextSkipCmd;
	}

	public short getSectionRunAdjustCmd() {
		return sectionRunAdjustCmd;
	}

	public void setSectionRunAdjustCmd(short sectionRunAdjustCmd) {
		this.sectionRunAdjustCmd = sectionRunAdjustCmd;
	}

	public short getDetainCmd() {
		return detainCmd;
	}

	public void setDetainCmd(short detainCmd) {
		this.detainCmd = detainCmd;
	}

	public short getTurnbackCmd() {
		return turnbackCmd;
	}

	public void setTurnbackCmd(short turnbackCmd) {
		this.turnbackCmd = turnbackCmd;
	}

	public short getBackDepotCmd() {
		return backDepotCmd;
	}

	public void setBackDepotCmd(short backDepotCmd) {
		this.backDepotCmd = backDepotCmd;
	}

	public short getDoorctrlStrategy() {
		return doorctrlStrategy;
	}

	public void setDoorctrlStrategy(short doorctrlStrategy) {
		this.doorctrlStrategy = doorctrlStrategy;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

}
