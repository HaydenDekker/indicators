package com.hdekker.indicators.indicator.state;

/**
 * Represent state used by this
 * package.
 * 
 * Immutable
 * 
 * Can insert DB update later on.
 * 
 * @author HDekker
 *
 * @param <T>
 */
public class IndicatorStateManager<T> {

	T state;
	
	public IndicatorStateManager(T state){
		this.state = state;
	}
	
	public T getState() {
		return state;
	}	
	
}
