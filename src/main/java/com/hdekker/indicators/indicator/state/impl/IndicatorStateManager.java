package com.hdekker.indicators.indicator.state.impl;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class IndicatorStateManager extends State<Map<String, Tuple2<MutableAttributeStateHolder, Integer>>>{

	public IndicatorStateManager(Map<String, Tuple2<MutableAttributeStateHolder, Integer>> state) {
		super(state);
		
	}
	
	public static Function<Tuple3<String, String, String>, String> confItemToInternalStateKey =
			(in) -> in.getT1() + "-" + in.getT2() + "-" + in.getT3();
		
	
	public static Function<Tuple3<String, String, String>, 
		Tuple2<String, Tuple2<MutableAttributeStateHolder, Integer>>> withNewInternalState = 
			(in) -> {
				
				IndicatorAttributeState iis = new IndicatorAttributeState(Map.of());
				MutableAttributeStateHolder msh = new MutableAttributeStateHolder();
				msh.setState(iis);
				// TODO dynamic chain number, set to 0.
				return Tuples.of(confItemToInternalStateKey.apply(in), Tuples.of(msh, 0));
				
	};
	
	public static Function<Map<String, Tuple2<MutableAttributeStateHolder, Integer>>,
		Function<Tuple3<String, String, String>,
			Tuple2<String, Tuple2<MutableAttributeStateHolder, Integer>>>> withExistingInternalState = 
			(eState) -> (in) -> {
				String key = confItemToInternalStateKey.apply(in);
				return Tuples.of(key, Tuples.of(eState.get(key).getT1(), 0));
			};
	public static Function<
		Function<Tuple3<String, String, String>,
			Tuple2<String, Tuple2<MutableAttributeStateHolder, Integer>>>,
		Function<List<Tuple3<String, String, String>>,
			Map<String, Tuple2<MutableAttributeStateHolder, Integer>>>> getInternalStateMap = (conv)->(list)->{
				return list.stream().map(conv)
					.collect(Collectors.toMap(i-> i.getT1(), i-> i.getT2()));
	};
			
			
			
}
