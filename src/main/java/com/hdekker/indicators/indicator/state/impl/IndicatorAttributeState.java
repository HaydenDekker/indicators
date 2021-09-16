package com.hdekker.indicators.indicator.state.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.hdekker.indicators.indicator.state.State;

/**
 * Immutable state, used by 
 * transforms and alerts.
 * 
 * 15-09-2021 - builder not helpful, removed.
 * design should be that indicators specify
 * configurable properties.
 * 
 * @author HDekker
 *
 */
public class IndicatorAttributeState extends State<Map<String, Double>>{

	//public final String fnId;
	
	public IndicatorAttributeState() {
		super(Map.of());
	}
	
	public IndicatorAttributeState(Map<String, Double> state) { // String fnId, 
		super(state);
		//this.fnId = fnId;
	}
	
	public IndicatorAttributeState copyAndPutAll(Map<String, Double> map){
		HashMap<String, Double> newMap = new HashMap<String, Double>(this.getState());
		newMap.putAll(map);
		return new IndicatorAttributeState(newMap);
	}
	
//	
//	public static class IndicatorInternalStateBuilder {
//		
//		Map<String, Object> state;
//		String keyPrefix;
//		
//		public IndicatorInternalStateBuilder(String keyPrefix, Map<String, Object> existingState) {
//			this.keyPrefix = keyPrefix;
//			this.state = new HashMap<String, Object>(existingState);
//		}
//		
//		public IndicatorInternalStateBuilder put(String key, Object value) {
//			state.put(keyPrefix + "-" + key, value);
//			return this;
//		}
//		
//		public IndicatorAttributeState build(){
//			return new IndicatorAttributeState(keyPrefix, Map.copyOf(state));
//		}
//		
//	}
//	
//	public static IndicatorInternalStateBuilder builder(String keyPrefix) {
//		return new IndicatorInternalStateBuilder(keyPrefix, Map.of());
//	}
//	
//	public static IndicatorInternalStateBuilder builder(String keyPrefix, Map<String, Object> state) {
//		return new IndicatorInternalStateBuilder(keyPrefix, state);
//	}
//	
//	public IndicatorInternalStateBuilder builder() {
//		return new IndicatorInternalStateBuilder(fnId, this.getState());
//						
//	}
	
	public Function<String, Double> stateReader(String indicatorId){
		return (key) -> {
			return getState().get(indicatorId + "-" + key);
		};
	}
	
//	/**
//	 * Changes the context of reads and writes
//	 * from within an indicator function so that
//	 * the fn itself is not aware of the unique id
//	 * provided by the overall indicator fn. 
//	 * 
//	 * Aids in chaining multiple fn's of same type
//	 * Enables a single map can be segregated by id prefix.
//	 * 
//	 * @param fnId
//	 * @return
//	 */
//	public IndicatorAttributeState bindTo(String fnId) {
//		return builder(fnId, getState()).build();
//	}

}
