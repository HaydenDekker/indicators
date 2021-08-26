package com.hdekker.indicators.indicator;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.Threshold;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;
import com.hdekker.indicators.indicator.transform.RSI;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public interface IndicatoryFactory {

	public static final String RSI_14_THRESH_BELOW30 = "RSI-14-ThreshBelow30";
	
	Map<String, Indicator> configuredIndicators = Map.of(
							RSI_14_THRESH_BELOW30, AppliedIndicators.rsi14MovesBelow30());
	
	public static Indicator getIndicator(String name) {
		return configuredIndicators.get(name);
	}
	
	public static Set<String> getIndicatorNames(){
		return configuredIndicators.keySet();
	}
	
	public static interface AppliedIndicators{
		
		public static Function<Tuple3<Integer, Double, IndicatorInternalState>, 
		Tuple2<Double, IndicatorInternalState>> convertInput = (in) -> Tuples.of(in.getT2(),  in.getT3());
	
		public static Function<String, 
			Function<Tuple2<Double, IndicatorInternalState>, Tuple2<Double, IndicatorInternalState>>> bindTo = (bind) -> (input) -> Tuples.of(input.getT1(),  input.getT2().bindTo(bind));
		
		
		public static Indicator rsi14MovesBelow30() {
			
			IndicatorInternalState conf = IndicatorInternalState.builder("rsi-fn-1")
					.put("steps", 14.00)
					.build()
					.bindTo("thresh-alt")
					.builder()
					.put("threshold", 30.00)
					.build()
					.bindTo("rsi-fn-1");
			
			IndicatorTransform rsiT = RSI.getTransform().withConfig(conf);
			
			IndicatorAlert ia = Threshold.movesBelowThresholdAlert()
					.withConfig(conf.bindTo("thresh-alt"));

		    Indicator ind = (input) -> convertInput
		    								.andThen(bindTo.apply("rsi-fn-1"))
		    								.andThen(rsiT)
		    								.andThen(bindTo.apply("thresh-alt"))
		    								.andThen(ia)
		    								.apply(input);
			
			return  ind;
		}
		
		
	}
	
	
}
