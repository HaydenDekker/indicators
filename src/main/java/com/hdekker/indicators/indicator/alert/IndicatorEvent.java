package com.hdekker.indicators.indicator.alert;

public class IndicatorEvent{
	
	final Double value;
	final String alert;
	
	public IndicatorEvent(Double value, String alert) {
		super();
		this.value = value;
		this.alert = alert;
	}
	
	public Double getValue() {
		return value;
	}
	public String getAlert() {
		return alert;
	}
	
	
	
}
