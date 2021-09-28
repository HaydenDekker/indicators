package com.hdekker.indicators.indicator.transform;


import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.hdekker.indicators.indicator.fn.ConfigurableIndicatorFn;
import com.hdekker.indicators.indicator.fn.IndicatorTransform;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;


import reactor.util.function.Tuples;

public class RSI{
	
	private static final String CONFIG_STEPS = "steps";
	
	public static final String aveGain = "rsi-ave-gain";
	public static final String aveLoss = "rsi-ave-loss";
	public static final String rsi = "rsi-rsi";
	public static final String value = "rsi-value";
	

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
			
	public static ConfigurableIndicatorFn<IndicatorTransform> getTransform() {
		
		return (conf)-> {
		
			String id = conf.getIndicatorFnId();
		    Double steps = (Double) Optional.of(conf.getConfig().get(CONFIG_STEPS)).get();
			Function<WilderRSIInstant, Function<Double, WilderRSIInstant>> wc = wilderCalculator(steps.intValue());
		
			return (input) -> {
			
				// may not have previous state so check values
				Double aveGainD = valueOrZero().apply(input.getT2().stateReader(id).apply(aveGain));						
				Double aveLossD = valueOrZero().apply(input.getT2().stateReader(id).apply(aveLoss));
				Double rsiD = valueOrZero().apply(input.getT2().stateReader(id).apply(rsi));
				Double valueD = valueOrZero().apply(input.getT2().stateReader(id).apply(value));
				
				WilderRSIInstant wri = new WilderRSIInstant();
				wri.setAvGain(aveGainD);
				wri.setAvLoss(aveLossD);
				wri.setRsi(rsiD);
				wri.setValue(valueD);
				
				WilderRSIInstant out = wc
						.apply(wri).apply(input.getT1());
				
				IndicatorAttributeState newState = input.getT2().copyAndPutAll(Map.of(
						 id + '-' + aveGain, out.getAvGain(),
						 id + '-' + aveLoss, out.getAvLoss(),
						 id + '-' + rsi, out.getRsi(),
						 id + '-' + value, out.getValue()
						));
				
				return Tuples.of(out.getRsi(), newState);
			};
		};
	}
		
}
