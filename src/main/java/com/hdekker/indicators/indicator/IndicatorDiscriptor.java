package com.hdekker.indicators.indicator;

import java.util.Map;

/**
 * 
 * Describes an indicator
 * implementation
 * 
 * 
 * @author HDekker
 *
 */
public class IndicatorDiscriptor {

	final String indicatorName;
	
	// TODO doesn't work well with
	// multiple internal functions and alerts
	// with multipl paths... hmm
	// potentiall the indicator interface
	// forces creation of one of these per
	// indicator for customisable properties.
	final Map<String,Double> indicatorConfiguration;
	
	public IndicatorDiscriptor(String indicatorName, Map<String, Double> indicatorConfiguration) {
		super();
		this.indicatorName = indicatorName;
		this.indicatorConfiguration = indicatorConfiguration;
	}

	public String getIndicatorName() {
		return indicatorName;
	}
	
	public Map<String, Double> getIndicatorConfiguration() {
		return indicatorConfiguration;
	}

}
