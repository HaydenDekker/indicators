package com.hdekker.indicators.indicator.state.impl;

public class MutableInternalStateHolder {

	IndicatorInternalState state;

	public IndicatorInternalState getState() {
		return state;
	}

	public void setState(IndicatorInternalState state) {
		this.state = state;
	}
	
}
