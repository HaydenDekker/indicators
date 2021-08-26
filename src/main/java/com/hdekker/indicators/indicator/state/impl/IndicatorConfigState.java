package com.hdekker.indicators.indicator.state.impl;

import java.util.List;
import java.util.Map;

import com.hdekker.indicators.indicator.Indicator;
import com.hdekker.indicators.indicator.state.IndicatorStateManager;

public class IndicatorConfigState extends IndicatorStateManager<Map<String, Map<String, List<String>>>>{

	public IndicatorConfigState(){
		super(Map.of());
	}
	public IndicatorConfigState(Map<String, Map<String, List<String>>> state) {
		super(state);
	}

}
