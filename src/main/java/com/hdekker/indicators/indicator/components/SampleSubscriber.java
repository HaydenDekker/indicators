package com.hdekker.indicators.indicator.components;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hdekker.indicators.indicator.IndicatorSampleData;
import com.hdekker.indicators.indicator.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.IndicatorFactory;
import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.state.impl.ConfigStateReader;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.state.impl.InternalStateReader;
import com.hdekker.indicators.indicator.state.impl.MutableAttributeStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * For a sample there may be a
 * map of secondary keys to indicators.
 * 
 * this component is expected to test the
 * sample against an indicator and potential 
 * secondary indicators producing alerts as detected.
 * 
 * @author HDekker
 *
 * @param <K>
 */
public interface SampleSubscriber<K extends IndicatorSampleData> {

	Flux<List<Tuple3<IndicatorEvent, K, IndicatorDetails>>> withInputs(Tuple3<
						Flux<K>,
						ConfigStateReader,
						InternalStateReader
						> samplesAndConfig);
	
	public class IndicatorDetails {
		
		// Previous state of indicator for asset PK and SK 
		final String stateKey;
		final String indicatorKey;
		final String sK;
		
		public IndicatorDetails(String stateKey, String indicatorKey, String sK) {
			super();
			this.stateKey = stateKey;
			this.indicatorKey = indicatorKey;
			this.sK = sK;
		}

		public String getStateKey() {
			return stateKey;
		}

		public String getIndicatorKey() {
			return indicatorKey;
		}

		public String getsK() {
			return sK;
		}
		
		
	}
	
	public static <K extends IndicatorSampleData> SampleSubscriber<K> builder() {
		
		return (tuple3)->{
			
			Function<List<IndicatorDetails>, List<Tuple2<IndicatorDetails, Tuple2<MutableAttributeStateHolder, Integer>>>> stateGetter = getIndicatorFnAndState.apply(tuple3.getT3());
			//reactive
			return tuple3.getT1().map(sample->{
				
				Function<List<Tuple2<IndicatorDetails, Tuple2<MutableAttributeStateHolder, Integer>>>, List<Tuple2<IndicatorEvent, IndicatorDetails>>> sub = computeIndicatorsAndUpdateMatchingIndicatorState.apply(sample.getSecondaryKey()).apply(sample.getValue());
				
				Optional<Map<String, List<String>>> optMap =  tuple3.getT2().apply(sample.getPrimaryKey());
				
				return optMap.map(map-> convertToIndStateKeys.apply(sample.getPrimaryKey(), map))
						.map(stateGetter)
						.map(sub)
						.map(iel->  iel.stream().map(ie-> Tuples.of(ie.getT1(), sample, ie.getT2())).collect(Collectors.toList()))
						.orElse(Arrays.asList());
				
			})
			.filter(l->l.size()>0); // don't need event if it failed.
			
		};
		
	}
	
	Function<String,
	Function<Double,
	Function<List<Tuple2<IndicatorDetails, Tuple2<MutableAttributeStateHolder, Integer>>>,
			List<Tuple2<IndicatorEvent, IndicatorDetails>>>>> computeIndicatorsAndUpdateMatchingIndicatorState
			 = (skToUpdate) -> (newValue) -> (inds) -> {
				 
				 return inds.stream().map(ind->{
					Tuple2<Optional<IndicatorEvent>, IndicatorAttributeState> res = IndicatorFactory.getIndicator(ind.getT1().getIndicatorKey())
					 			.test(new IndicatorTestSpec(0, newValue, ind.getT2().getT1().getState()));
					
					if(skToUpdate.equals(ind.getT1().getsK())) ind.getT2().getT1().setState(res.getT2());
					
					return res.getT1().map(ie-> Tuples.of(ie, ind.getT1()));
				 })
				 .filter(Optional::isPresent)
				 .map(Optional::get)
				 .collect(Collectors.toList());
				 
			 };
	
			 // returns list stateKey, indicatorKey, and SK for each indicator
	public Function<Tuple3<String, String, List<String>>
					,List<IndicatorDetails>> getIndicatorStateKeys
					= (in) -> in.getT3().stream().map(s-> new IndicatorDetails(in.getT1() + "-" + in.getT2() + "-" + s, s, in.getT2())).collect(Collectors.toList());
	
	BiFunction<String,Map<String, List<String>>, List<IndicatorDetails>> convertToIndStateKeys
				 = (pk, map) -> map.keySet()
							.stream()
							.flatMap(sk-> getIndicatorStateKeys
											.apply(Tuples.of(pk, sk, map.get(sk)))
											.stream())
							.collect(Collectors.toList());
			
	Function<InternalStateReader,
	Function<List<IndicatorDetails>,
				List<Tuple2<IndicatorDetails, Tuple2<MutableAttributeStateHolder, Integer>>>>> getIndicatorFnAndState
				 = (reader) -> (keys) -> keys.stream()
					.map(key-> Tuples.of(key, reader.apply(key.getStateKey())))
					.collect(Collectors.toList());
	
				 
}
