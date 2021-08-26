package com.hdekker.indicators.indicator;

import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

/**
 * Interface for transforms and alerts
 * to configure there internal structure.
 * 
 * @author HDekker
 *
 * @param <T>
 */
public interface IndicatorFnConfig<T> {

	public T withConfig(IndicatorInternalState config);
	
}
