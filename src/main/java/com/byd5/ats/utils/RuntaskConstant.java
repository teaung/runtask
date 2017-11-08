package com.byd5.ats.utils;

/**
 * 运行任务模块：常量类
 * @author wu.xianglan
 *
 */
public class RuntaskConstant {

	//---------------------------------exchange----------------------------------------
	
	/**
	 * 运行任务模块的topic
	 */
	public final static String RABB_EX_DEPART = "topic.ats.traindepart";
	/**
	 * 运行图模块的topic
	 */
	public final static String RABB_EX_RUNGRAPH = "topic.ats.trainrungraph";
	/**
	 * 服务端发给客户端的topic
	 */
	public final static String RABB_EX_SER2CLI = "topic.serv2cli";
	/**
	 * 识别跟踪模块的topic
	 */
	public final static String RABB_EX_TRACE = "topic.ats.traintrace";
	/**
	 * 运行调整模块的topic
	 */
	public final static String RABB_EX_DJUST = "topic.ats.trainadjust";
	
	
	//---------------------------------路由key----------------------------------------
	
	/**
	 * 运行调整的路由key
	 */
	public final static String RABB_RK_ADJUST_RUNTIME = "ats.trainadjust.runtime";
	/**
	 * 运行图的运行任务的路由key
	 */
	public final static String RABB_RK_RUNGRAPH_TASK = "ats.trainrungraph.task";
	/**
	 * 运行图的出段信息的路由key
	 */
	public final static String RABB_RK_RUNGRAPH_OUTGARAGE = "ats.trainrungraph.runOutgarage";
	/**
	 * 到站(停稳)消息的路由key
	 */
	public final static String RABB_RK_TRACE_ARRIVE_STATION = "ats.traintrace.station.arrive";
	/**
	 * 到站(非停稳)消息的路由key
	 */
	public final static String RABB_RK_TRACE_ENTER_STATION = "ats.traintrace.station.enter";
	/**
	 * 离开折返轨消息的路由key
	 */
	public final static String RABB_RK_TRACE_LEAVE_RETURN = "ats.traintrace.return.leave";
	/**
	 * 到达转换轨消息的路由key
	 */
	public final static String RABB_RK_TRACE_ARRIVE_TRANSFORM = "ats.traintrace.transform.arrive";
	/**
	 * 告警消息的路由key
	 */
	public final static String RABB_RK_ALARM_ALERT = "ats.trainrungraph.alert";
	/**
	 * 发车倒计时消息的路由key
	 */
	public final static String RABB_RK_RUNTASK_REALTIME = "serv2cli.trainruntask.realtime";
	/**
	 * AOD命令消息的路由key
	 */
	public final static String RABB_RK_RUNTASK_AOD = "ats.traindepart.aod.command";
	
	
	//---------------------------------restful URL----------------------
	/**
	 * 获取运行控制模块的站台跳停状态
	 */
	public final static String HX_CONTROL_SKIP_STATUS = "http://serv35-traincontrol/SkipStationStatus/info?stationId={stationId}";
	/**
	 * 获取运行控制模块的站台停站状态
	 */
	public final static String HX_CONTROL_DWELL_TIME = "http://serv35-traincontrol/DwellTime/info?platformId={platformId}";
	/**
	 * 获取运行图模块的所有停站时间
	 */
	public final static String HX_RUNGRAPH_DWELL_ALL = "http://serv31-trainrungraph/server/getDwellTime";
	/**
	 * 保存停站时间
	 */
	public final static String HX_RUNGRAPH_DWELL_UPDATE = "http://serv31-trainrungraph/server/saveRuntaskCommand?json={json}";
	/**
	 * 获取运行图模块的运行任务
	 */
	public final static String HX_RUNGRAPH_TASK = "http://serv31-trainrungraph/server/getRuntask?groupnum={groupnum}&tablenum={tablenum}&trainnum={trainnum}";

	/**
	 * 线路编号ID
	 */
	public final static Short NID_LINE = 64;
	/**
	 * 默认停站时间（单位:s）
	 */
	public final static Integer DEF_DWELL_TIME = 30;
}
