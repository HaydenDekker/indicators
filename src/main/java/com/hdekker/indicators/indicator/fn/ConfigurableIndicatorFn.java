package com.hdekker.indicators.indicator.fn;

import com.hdekker.indicators.indicator.IndicatorFnConfigSpec;

/**
 * Interface for transforms and alerts
 * to configure their internal structure.
 * 
 * @author HDekker
 *
 * @param <T>
 */
public interface ConfigurableIndicatorFn<T> {

	public T withConfig(IndicatorFnConfigSpec spec);
	
	
}
