package com.byd5.ats.utils;


/**
 * 消息队列所用常量类
 * @author wu.xianglan
 *
 */
public class RuntaskConstant {

	/**
	 ***************************************************************
	 * 消息队列所用常量定义
	 ***************************************************************
	 */
	/**
	 * RabbitMQ's Exchange
	 */
	/** 服务端->客户端*/
	public final static String RABB_EX_SER2CLI = "topic.serv2cli";
	/** 运行图*/
	public final static String RABB_EX_RUNGRAPH = "topic.ats.trainrungraph";
	/** 识别跟踪*/
	public final static String RABB_EX_TRACE = "topic.ats.traintrace";
	
	public final static String RABB_EX_RUNTASK = "topic.ats.traindepart";
	/** 运行调整*/
	public final static String RABB_EX_ADJUST = "topic.ats.trainadjust";

	/**
	 * RabbitMQ's RoutingKey: adjust
	 */
	/**
	 * 信息类型编号   信息包名称         发送方向            字节长度     发送方式<br>
	 * 0x0203    ATO命令信息帧  ATS->VOBC  4~42         非周期
	 */
	/** 运行调整runtime*/
	public final static String RABB_RK_ADJUST_RUNTIME = "ats.trainadjust.runtime";
	/** 运行图task*/
	public final static String RABB_RK_RUNGRAPH_TASK = "ats.trainrungraph.task";
	/** 运行图outgarage*/
	public final static String RABB_RK_RUNGRAPH_OUT = "ats.trainrungraph.runOutgarage";
	/** 识别跟踪：列车到站停稳*/
	public final static String RABB_RK_TRACE_SA = "ats.traintrace.station.arrive";
	/** 识别跟踪：列车到站未停稳*/
	public final static String RABB_RK_TRACE_SE = "ats.traintrace.station.enter";
	/** 识别跟踪：列车离开折返轨*/
	public final static String RABB_RK_TRACE_RL = "ats.traintrace.return.leave";
	
}
