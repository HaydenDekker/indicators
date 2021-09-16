package com.hdekker.indicators.indicator;

import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

/**
 * Interface for transforms and alerts
 * to configure their internal structure.
 * 
 * @author HDekker
 *
 * @param <T>
 */
public interface IndicatorFnConfig<T> {

	public T withConfig(IndicatorFnConfigSpec spec);
	
	/**
	 * To configure an indicator function 
	 * (sub component of an indicator)
	 * Component must have unique id
	 * and list of configurable properties
	 * 
	 * @author HDekker
	 *
	 */
	static class IndicatorFnConfigSpec {
		
		final String indicatorFnId;
		final IndicatorAttributeState config;
		
		
		public IndicatorFnConfigSpec(String indicatorFnId, IndicatorAttributeState config) {
			super();
			this.indicatorFnId = indicatorFnId;
			this.config = config;
		}
		public String getIndicatorFnId() {
			return indicatorFnId;
		}
		public IndicatorAttributeState getConfig() {
			return config;
		}

	}
	
}
