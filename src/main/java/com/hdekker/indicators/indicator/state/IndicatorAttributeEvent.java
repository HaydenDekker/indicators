package com.hdekker.indicators.indicator.state;

import java.time.LocalDateTime;
import java.util.Map;

public class IndicatorAttributeEvent {

	String indicatorKey;
	Map<String, Double> indicatorState;
	LocalDateTime updateTime;
	
	public String getIndicatorKey() {
		return indicatorKey;
	}
	public void setIndicatorKey(String indicatorKey) {
		this.indicatorKey = indicatorKey;
	}
	public Map<String, Double> getIndicatorState() {
		return indicatorState;
	}
	public void setIndicatorState(Map<String, Double> indicatorState) {
		this.indicatorState = indicatorState;
	}
	public LocalDateTime getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}
	
}
