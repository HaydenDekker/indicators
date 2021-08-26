package com.hdekker.indicators.indicator.state.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.hdekker.indicators.indicator.state.IndicatorStateManager;

/**
 * Immutable state, used by 
 * transforms and alerts.
 * 
 * @author HDekker
 *
 */
public class IndicatorInternalState extends IndicatorStateManager<Map<String, Object>>{

	public final String fnId;
	
	private IndicatorInternalState(String fnId, Map<String, Object> state) {
		super(state);
		this.fnId = fnId;
	}
	
	public static class IndicatorInternalStateBuilder {
		
		Map<String, Object> state;
		String keyPrefix;
		
		public IndicatorInternalStateBuilder(String keyPrefix, Map<String, Object> existingState) {
			this.keyPrefix = keyPrefix;
			this.state = new HashMap<String, Object>(existingState);
		}
		
		public IndicatorInternalStateBuilder put(String key, Object value) {
			state.put(keyPrefix + "-" + key, value);
			return this;
		}
		
		public IndicatorInternalState build(){
			return new IndicatorInternalState(keyPrefix, Map.copyOf(state));
		}
		
	}
	
	public static IndicatorInternalStateBuilder builder(String keyPrefix) {
		return new IndicatorInternalStateBuilder(keyPrefix, Map.of());
	}
	
	public static IndicatorInternalStateBuilder builder(String keyPrefix, Map<String, Object> state) {
		return new IndicatorInternalStateBuilder(keyPrefix, state);
	}
	
	public IndicatorInternalStateBuilder builder() {
		return new IndicatorInternalStateBuilder(fnId, this.getState());
						
	}
	
	public Function<String, Object> stateReader(){
		return (key) -> {
			return getState().get(fnId + "-" + key);
		};
	}
	
	/**
	 * Changes the context of reads and writes
	 * from within an indicator function so that
	 * the fn itself is not aware of the unique id
	 * provided by the overall indicator fn. 
	 * 
	 * Aids in chaining multiple fn's of same type
	 * Enables a single map can be segregated by id prefix.
	 * 
	 * @param fnId
	 * @return
	 */
	public IndicatorInternalState bindTo(String fnId) {
		return builder(fnId, getState()).build();
	}

}
