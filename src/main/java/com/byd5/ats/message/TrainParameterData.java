package com.byd5.ats.message;

/**
 * 获取参数管理数据
 * 
 * @author wu.xianglan
 */
public class TrainParameterData {

	private String parameterKey;// 参数key

	private String parameterName;// 参数名

	private String parameterType;// 参数类型

	private Long minValue;// 最小值

	private Long maxValue;// 最大值

	private Long tepValue;// 当前使用值

	public String getParameterKey() {
		return parameterKey;
	}

	public void setParameterKey(String parameterKey) {
		this.parameterKey = parameterKey;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public Long getMinValue() {
		return minValue;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	public Long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	public Long getTepValue() {
		return tepValue;
	}

	public void setTepValue(Long tepValue) {
		this.tepValue = tepValue;
	}

}