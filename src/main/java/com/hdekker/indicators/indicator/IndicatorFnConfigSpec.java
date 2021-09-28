package com.hdekker.indicators.indicator;

import java.util.Map;

import com.hdekker.indicators.indicator.IndicatorFnDescriptor.IndicatorFNType;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

/**
 * To configure an indicator function 
 * (sub component of an indicator)
 * Component must have unique id
 * and list of configurable properties
 * 
 * @author HDekker
 *
 */
public class IndicatorFnConfigSpec {
	
	/**
	 * Custom-ID for fn as potentially multiple
	 * fn's of same type and identity could be chained
	 */
	final String indicatorFnId;
	final Map<String, Double> config;
	final IndicatorFnIdentity fnIdentity;
	final IndicatorFNType fnType;
	
	public IndicatorFnConfigSpec(String indicatorFnId, Map<String, Double> config, IndicatorFNType fnType, IndicatorFnIdentity fnIdentity) {
		super();
		this.indicatorFnId = indicatorFnId;
		this.config = config;
		this.fnIdentity = fnIdentity;
		this.fnType = fnType;
	}
	
	public Map<String, Double> getConfig() {
		return config;
	}


	public IndicatorFNType getFnType() {
		return fnType;
	}


	public String getIndicatorFnId() {
		return indicatorFnId;
	}

	public IndicatorFnIdentity getFnIdentity() {
		return fnIdentity;
	}
	
	

}
