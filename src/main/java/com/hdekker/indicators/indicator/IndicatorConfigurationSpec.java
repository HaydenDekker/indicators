package com.hdekker.indicators.indicator;

import java.time.LocalDateTime;
import java.util.List;

import com.hdekker.indicators.indicator.fn.Indicator;

/**
 * Indicator consists of a number of fn's
 * chained together with a final alert stage
 * 
 * @author Hayden Dekker
 *
 */
public class IndicatorConfigurationSpec {

	final String indicatorName;
	final List<IndicatorFnConfigSpec> configuration;
	
	public IndicatorConfigurationSpec(String indicatorName, List<IndicatorFnConfigSpec> configuration) {
		super();
		this.indicatorName = indicatorName;
		this.configuration = configuration;
	}
	public String getIndicatorName() {
		return indicatorName;
	}
	public List<IndicatorFnConfigSpec> getConfiguration() {
		return configuration;
	}
	
	
	
}
