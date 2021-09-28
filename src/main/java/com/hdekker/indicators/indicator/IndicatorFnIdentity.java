package com.hdekker.indicators.indicator;

public enum IndicatorFnIdentity {

	ALERT_THRESHOLD_BELOW("Drops below threshold"),
	ALERT_THRESHOLD_ABOVE("Rises above threshold"),
	
	TRANSFORM_RSI("RSI");
	
	public final String displayText;
	
	private IndicatorFnIdentity(String displayText) {
		this.displayText = displayText;
	}
	
}
