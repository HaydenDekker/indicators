package com.hdekker.indicators.indicator;


public class IndicatorConfiguration {

	final IndicatorDiscriptor indicatorDiscriptor;
	final Indicator indicator;

	public IndicatorConfiguration(IndicatorDiscriptor indicatorDiscriptor, Indicator indicator) {
		super();
		this.indicatorDiscriptor = indicatorDiscriptor;
		this.indicator = indicator;
	}

	public IndicatorDiscriptor getIndicatorDiscriptor() {
		return indicatorDiscriptor;
	}

	public Indicator getIndicator() {
		return indicator;
	}
	
	
	
}
