package com.hdekker.indicators.indicator.state;

/**
 * Represent state used by this
 * package.
 * 
 * Immutable
 * 
 * Can insert DB update later on.
 * 
 * 
 * @author HDekker
 *
 * @param <T>
 */
public class State<T> {

	T state;
	
	public State(T state){
		this.state = state;
	}
	
	public T getState() {
		return state;
	}	
	
}
