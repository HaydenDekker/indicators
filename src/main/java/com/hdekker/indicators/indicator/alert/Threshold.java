package com.hdekker.indicators.indicator.alert;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.hdekker.indicators.indicator.fn.ConfigurableIndicatorFn;
import com.hdekker.indicators.indicator.fn.IndicatorAlert;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

import reactor.util.function.Tuples;

public class Threshold {
	
	public static final String VALUE = "value";
	public static final String PREV_STATE = "prev-state";
	
	static Function<Double, BiPredicate<Double, Double>> movedAboveThreshold = (threshold) -> (current, previous) -> (current>threshold&&previous<threshold);
	static Function<Double, BiPredicate<Double, Double>> movedBelowThreshold = (threshold) -> (current, previous) -> (current<threshold&&previous>threshold);						
	
	public static ConfigurableIndicatorFn<IndicatorAlert> movesAboveThresholdAlert() {
			
		return thresholdAlertBuilder()
					.apply("Value moved above set threshold")
					.apply(movedAboveThreshold);
				
	}
	
	static Function<String, 
	Function<Function<Double, BiPredicate<Double, Double>>, ConfigurableIndicatorFn<IndicatorAlert>>>
		thresholdAlertBuilder(){
		
		return (alertText) -> (thresholdFnBuilder) -> (conf) -> { 
		
			String id = conf.getIndicatorFnId();
			Double threshold = (Double) Optional.of(conf.getConfig().get(VALUE)).get();
			BiPredicate<Double, Double> thresholdFn = thresholdFnBuilder.apply(threshold);
			
			return (d)-> {
				
			// create new state
			IndicatorAttributeState newState = d.getT2().copyAndPutAll(Map.of(
					 id + "-" + PREV_STATE, d.getT1()
					));
	
			// test against threshold
			// TODO 16-09 the fn's should care about the fn id internally.
			// almost need to go to a list of fn attribute maps.
			Optional<IndicatorEvent> ie = Optional.ofNullable(d.getT2().stateReader(id).apply(PREV_STATE))
							.map(o-> thresholdFn.test(d.getT1(), (Double) o ))
							.filter(b-> b.equals(true))
							.map(b-> new IndicatorEvent(d.getT1(), alertText + " " + threshold)); // new event
							
			return Tuples.of(ie, newState);
		    
			};
		};

	}
					
	
	
	public static ConfigurableIndicatorFn<IndicatorAlert> movesBelowThresholdAlert() {
						
		return thresholdAlertBuilder().apply("Value moved below set threshold")
										.apply(movedBelowThreshold);
	}
}
