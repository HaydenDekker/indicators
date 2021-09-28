package com.hdekker.indicators.indicator;

import java.util.Map;

/**
 * 
 * Describes an indicator's component
 * implementation
 * 
 * 
 * @author HDekker
 *
 */
public class IndicatorFnDescriptor {

	final IndicatorFnIdentity indicatorName;
	final Map<String,Double> indicatorConfiguration;
	final IndicatorFNType fnType;

	public enum IndicatorFNType {
		
		Transform,
		Alert
		
	}
	
	public IndicatorFnDescriptor(IndicatorFnIdentity indicatorName, Map<String, Double> indicatorConfiguration, IndicatorFNType fnType) {
		this.indicatorName = indicatorName;
		this.indicatorConfiguration = indicatorConfiguration;
		this.fnType = fnType;
		
	}
	
	public IndicatorFNType getFnType() {
		return fnType;
	}

	public IndicatorFnIdentity getIndicatorName() {
		return indicatorName;
	}
	
	public Map<String, Double> getIndicatorConfiguration() {
		return indicatorConfiguration;
	}

}
