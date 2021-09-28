package com.hdekker.indicators.indicator.state.impl;

import java.util.List;
import java.util.Map;

import com.hdekker.indicators.indicator.fn.Indicator;
import com.hdekker.indicators.indicator.state.State;

public class IndicatorConfigState extends State<Map<String, Map<String, List<String>>>>{

	public IndicatorConfigState(){
		super(Map.of());
	}
	public IndicatorConfigState(Map<String, Map<String, List<String>>> state) {
		super(state);
	}

}
