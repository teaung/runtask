package com.byd5.ats.message;


public class TrainEventPosition {
	
	private Integer service_num;/* 服务号/表号 */
	private Integer line_num;/* 线路编号 */
	private Integer train_line_num;/* 车组所属线路编号 */
	private Integer train_num;/* 车组号 */
	private Integer origin_line_num;/* 列车始发站线路编号*/
	private Integer train_order_num;/* 车次号 */
	private Integer destin_line_num;/* 目的地线路编号*/
	private Integer destin_num; /* 目的地号*/
	private Integer direction_train;// 运行方向|1-下行，2-上行|
	private Integer park_stab_status;//列车停稳状态
	private Integer this_station_id;//这一站台id
	private Integer next_station_id;//下一站台id
	private Integer  t_head_track_id;//车头所在的逻辑区段_
	private Integer running_level;//运行等级
	private Long sec;//秒
	private Long usec;//微秒
	
	public Integer getService_num() {
		return service_num;
	}
	public void setService_num(Integer service_num) {
		this.service_num = service_num;
	}
	public Integer getLine_num() {
		return line_num;
	}
	public void setLine_num(Integer line_num) {
		this.line_num = line_num;
	}
	public Integer getTrain_line_num() {
		return train_line_num;
	}
	public void setTrain_line_num(Integer train_line_num) {
		this.train_line_num = train_line_num;
	}
	public Integer getTrain_num() {
		return train_num;
	}
	public void setTrain_num(Integer train_num) {
		this.train_num = train_num;
	}
	public Integer getOrigin_line_num() {
		return origin_line_num;
	}
	public void setOrigin_line_num(Integer origin_line_num) {
		this.origin_line_num = origin_line_num;
	}
	public Integer getTrain_order_num() {
		return train_order_num;
	}
	public void setTrain_order_num(Integer train_order_num) {
		this.train_order_num = train_order_num;
	}
	public Integer getDestin_line_num() {
		return destin_line_num;
	}
	public void setDestin_line_num(Integer destin_line_num) {
		this.destin_line_num = destin_line_num;
	}
	public Integer getDestin_num() {
		return destin_num;
	}
	public void setDestin_num(Integer destin_num) {
		this.destin_num = destin_num;
	}
	public Integer getDirection_train() {
		return direction_train;
	}
	public void setDirection_train(Integer direction_train) {
		this.direction_train = direction_train;
	}
	public Integer getPark_stab_status() {
		return park_stab_status;
	}
	public void setPark_stab_status(Integer park_stab_status) {
		this.park_stab_status = park_stab_status;
	}
	public Integer getThis_station_id() {
		return this_station_id;
	}
	public void setThis_station_id(Integer this_station_id) {
		this.this_station_id = this_station_id;
	}
	public Integer getNext_station_id() {
		return next_station_id;
	}
	public void setNext_station_id(Integer next_station_id) {
		this.next_station_id = next_station_id;
	}
	
	public Integer getT_head_track_id() {
		return t_head_track_id;
	}
	public void setT_head_track_id(Integer t_head_track_id) {
		this.t_head_track_id = t_head_track_id;
	}
	public Integer getRunning_level() {
		return running_level;
	}
	public void setRunning_level(Integer running_level) {
		this.running_level = running_level;
	}
	public Long getSec() {
		return sec;
	}
	public void setSec(Long sec) {
		this.sec = sec;
	}
	public Long getUsec() {
		return usec;
	}
	public void setUsec(Long usec) {
		this.usec = usec;
	}
	

}
