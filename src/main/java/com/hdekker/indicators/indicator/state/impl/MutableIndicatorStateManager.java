package com.hdekker.indicators.indicator.state.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.state.State;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * String containing the PK,SK and Indicator Id
 * 
 * Mutable holder allows map to remain immutable, guarantees concurrency.
 * 
 * @author HDekker
 *
 */
public class MutableIndicatorStateManager extends State<Map<String, IndicatorTestResult>>{

	public MutableIndicatorStateManager(Map<String, IndicatorTestResult> state) {
		// make mutable.
		super(new HashMap<>(state));
		
	}
	
	public static Function<Tuple3<String, String, String>, String> confItemToInternalStateKey =
			(in) -> in.getT1() + "-" + in.getT2() + "-" + in.getT3();
		
	/**
	 *  LocalDateTime for sample set way back, 2 years so it doesn't
	 *  interfere with indicator updates.
	 * 
	 */
	public static Function<Tuple3<String, String, String>, 
		Tuple2<String, IndicatorTestResult>> withNewInternalState = 
			(in) -> {
				
				IndicatorAttributeState iis = new IndicatorAttributeState(Map.of());
//				MutableAttributeStateHolder msh = new MutableAttributeStateHolder();
//				msh.setState(iis);
				return Tuples.of(confItemToInternalStateKey.apply(in), 
						new IndicatorTestResult(Optional.empty(),
							new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), iis)));
				
	};
	
	public static Function<Map<String, IndicatorTestResult>,
		Function<Tuple3<String, String, String>,
			Tuple2<String, IndicatorTestResult>>> withExistingInternalState = 
			(eStateMap) -> (in) -> {
				String key = confItemToInternalStateKey.apply(in);
				IndicatorTestResult eSpec = eStateMap.get(key); 
				return Tuples.of(key, 
					new IndicatorTestResult(
						eSpec.getOptEvent(),
					new IndicatorTestSpec(
						eSpec.getSpec().getInputPathNum(), 
						eSpec.getSpec().getValue(), 
						eSpec.getSpec().getSampleDate(), 
						eSpec.getSpec().getIndicatorAttributeState())));
			};
			
			
	public static <T, K> Function<
		Function<T,
			Tuple2<String, K>>,
		Function<List<T>,
			Map<String, K>>> getInternalStateMap(){
			
			return (conv)->(list)->{
				return list.stream()
						.map(conv)
						.collect(Collectors.toMap(i-> i.getT1(), i-> i.getT2()));
			};
	}
			
			
			
}
