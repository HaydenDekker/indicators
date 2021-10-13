package com.hdekker.indicators.indicator.state;

import java.time.LocalDateTime;
import java.util.Map;

import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;

public class IndicatorTestResultEvent {

	final String indicatorKey;
	final IndicatorTestResult result;
	final LocalDateTime updateTime;
	public IndicatorTestResultEvent(String indicatorKey, IndicatorTestResult result, LocalDateTime updateTime) {
		super();
		this.indicatorKey = indicatorKey;
		this.result = result;
		this.updateTime = updateTime;
	}
	public String getIndicatorKey() {
		return indicatorKey;
	}
	public IndicatorTestResult getResult() {
		return result;
	}
	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	
	
}
