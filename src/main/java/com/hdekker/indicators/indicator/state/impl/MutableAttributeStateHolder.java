package com.hdekker.indicators.indicator.state.impl;

public class MutableAttributeStateHolder {

	IndicatorAttributeState state;

	public IndicatorAttributeState getState() {
		return state;
	}

	public void setState(IndicatorAttributeState state) {
		this.state = state;
	}
	
}
