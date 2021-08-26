package com.hdekker.indicators.indicator.alert;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.hdekker.indicators.indicator.IndicatorAlert;
import com.hdekker.indicators.indicator.IndicatorFnConfig;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuples;

public interface Threshold {
	
	public static final String THRESHOLD = "threshold";
	public static final String PREV_STATE = "prev-state";
	
	Function<Double, BiPredicate<Double, Double>> movedAboveThreshold = (threshold) -> (current, previous) -> (current>threshold&&previous<threshold);
	Function<Double, BiPredicate<Double, Double>> movedBelowThreshold = (threshold) -> (current, previous) -> (current<threshold&&previous>threshold);						
	
	public static IndicatorFnConfig<IndicatorAlert> movesAboveThresholdAlert() {
			
		return thresholdAlertBuilder()
					.apply("Value moved above set threshold")
					.apply(movedAboveThreshold);
				
	}
	
	static Function<String, 
	Function<Function<Double, BiPredicate<Double, Double>>, IndicatorFnConfig<IndicatorAlert>>>
		thresholdAlertBuilder(){
		
		return (alertText) -> (thresholdFnBuilder) -> (conf) -> { 
		
			Function<String, Object> confStateReader = conf.stateReader();
			Double threshold = (Double) Optional.of(confStateReader.apply(THRESHOLD)).get();
			BiPredicate<Double, Double> thresholdFn = thresholdFnBuilder.apply(threshold);
			
			return (d)-> {
				
			// create new state
			IndicatorInternalState newState = d.getT2().builder()
								.put(PREV_STATE, d.getT1())
								.build();
							
			// test against threshold
			Optional<IndicatorEvent> ie = Optional.ofNullable(d.getT2().stateReader().apply(PREV_STATE))
							.map(o-> thresholdFn.test(d.getT1(), (Double) o ))
							.filter(b-> b.equals(true))
							.map(b-> new IndicatorEvent(d.getT1(), alertText + " " + threshold)); // new event
							
			return Tuples.of(ie, newState);
		    
			};
		};

	}
					
	
	
	public static IndicatorFnConfig<IndicatorAlert> movesBelowThresholdAlert() {
						
		return thresholdAlertBuilder().apply("Value moved below set threshold")
										.apply(movedBelowThreshold);
	}
}
