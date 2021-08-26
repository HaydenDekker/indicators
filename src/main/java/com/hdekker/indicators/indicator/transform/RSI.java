package com.hdekker.indicators.indicator.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hdekker.indicators.indicator.IndicatorFnConfig;
import com.hdekker.indicators.indicator.IndicatorTransform;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public interface RSI {
	
	public static Function<WilderRSIInstant, Function<Double, WilderRSIInstant>> wilderCalculator(Integer steps) {
		
		Double stepsAsDouble = Double.valueOf(steps);
		BiFunction<Double, Double, Double> averagesFn = calcWilderAve.apply(stepsAsDouble);
	
		return (state) -> {
				return (update) -> {
					
					Double currentGain = calcGain.apply(state.getValue(), update);
					Double currentLoss = calcLoss.apply(state.getValue(), update);
					
					Double periodAvGain = averagesFn.apply(state.getAvGain(), currentGain);
					Double periodAvLoss = averagesFn.apply(state.getAvLoss(), currentLoss);
					
					Double rsi = clacRSI.apply(periodAvGain,periodAvLoss);
					
					WilderRSIInstant wri = new WilderRSIInstant();
					wri.setAvGain(periodAvGain);
					wri.setAvLoss(periodAvLoss);
					wri.setRsi(rsi);
					wri.setValue(update);
					return wri;
				};
		};
	}

	public static Function<Double,BiFunction<Double, Double, Double>> calcWilderAve =
			(period)->(prev, curr)-> curr/period + (period-1)/period*prev;

	public static BiFunction<Double, Double, Double> calcGain = 
			(prev, current) -> (current>prev) ? current - prev: 0;
	public static BiFunction<Double, Double, Double> calcLoss = 
			(prev, current) -> (current<prev) ? -1* (current - prev): 0;
	
	/**
	 * 
	 *  
	 */
	public static BiFunction<Double, Double, Double> clacRSI = 
			(aveGain, aveLoss)-> 100 - 100/ ( 1+ aveGain/aveLoss);

	public static Function<Object, Double> valueOrZero() {
		return (value)-> Optional.ofNullable(value).map(o-> (Double) o).orElse(0.0);
	};
	
	public static final String aveGain = "rsi-ave-gain";
	public static final String aveLoss = "rsi-ave-loss";
	public static final String rsi = "rsi-rsi";
	public static final String value = "rsi-value";
	
			
	public static IndicatorFnConfig<IndicatorTransform> getTransform() {
		
		return (conf)-> {
		
		    Double steps = (Double) Optional.of(conf.stateReader().apply("steps")).get();
			Function<WilderRSIInstant, Function<Double, WilderRSIInstant>> wc = wilderCalculator(steps.intValue());
		
			return (input) -> {
			
				// may not have previous state so check values
				Double aveGainD = valueOrZero().apply(input.getT2().stateReader().apply(aveGain));						
				Double aveLossD = valueOrZero().apply(input.getT2().stateReader().apply(aveLoss));
				Double rsiD = valueOrZero().apply(input.getT2().stateReader().apply(rsi));
				Double valueD = valueOrZero().apply(input.getT2().stateReader().apply(value));
				
				WilderRSIInstant wri = new WilderRSIInstant();
				wri.setAvGain(aveGainD);
				wri.setAvLoss(aveLossD);
				wri.setRsi(rsiD);
				wri.setValue(valueD);
				
				WilderRSIInstant out = wc
						.apply(wri).apply(input.getT1());
				
				IndicatorInternalState newState = input.getT2().builder()
					.put(aveGain, out.getAvGain())
					.put(aveLoss, out.getAvLoss())
					.put(rsi, out.getRsi())
					.put(value, out.getValue())
					.build();
				
				return Tuples.of(out.getRsi(), newState);
			};
		};
	}
		
}
