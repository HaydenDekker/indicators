package com.hdekker.indicators.indicator;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.hdekker.indicators.indicator.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.IndicatorFnConfig.IndicatorFnConfigSpec;
import com.hdekker.indicators.indicator.alert.Threshold;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.transform.RSI;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public interface IndicatorFactory {

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
		
		public static Indicator rsi14MovesBelow30() {
			
			IndicatorAttributeState conf = new IndicatorAttributeState(Map.of(
					"rsi-fn-1-steps", 14.00,
					"thresh-alt-threshold", 30.00
					));
			
			IndicatorTransform rsiT = RSI.getTransform().withConfig(new IndicatorFnConfigSpec("rsi-fn-1", conf));
			
			IndicatorAlert ia = Threshold.movesBelowThresholdAlert()
					.withConfig(new IndicatorFnConfigSpec("thresh-alt", conf));

		    Indicator ind = (input) ->    rsiT
		    								.andThen(ia)
		    								.apply(Tuples.of(input.getValue(),  input.getIndicatorAttributeState()));
		    								// Tupl - drops path number... it's not in use currently 16-09
			return  ind;
		}
		
		
	}
	
	
}
