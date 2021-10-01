package com.hdekker.indicators.indicator.components;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.hdekker.indicators.indicator.IndicatorSampleData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hdekker.indicators.indicator.IndicatorFactory;
import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.state.impl.ConfigStateReader;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.state.impl.MutableIndicatorStateManager;

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
						Supplier<MutableIndicatorStateManager>
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
		
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());
		
		return (tuple3)->{
			
			Function<List<IndicatorDetails>, 
				List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>> 
					stateGetter = getIndicatorFnAndState.apply(tuple3.getT3());
			
			return tuple3.getT1()
					.map(sample->{
						
						Function<List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>, 
							List<Optional<Tuple2<IndicatorDetails,IndicatorTestResult>>>> 
							compInd = computeIndicators.apply(Tuples.of(sample.getValue(), sample.getSampleDateTime()));
								
						Optional<Map<String, List<String>>> optConfigMap =  tuple3.getT2().apply(sample.getPrimaryKey());
				
//						try {
//							LoggerFactory.getLogger(SampleSubscriber.class)
//								.info("Config found for sample sk " + om.writeValueAsString(optConfigMap.get()));
//						} catch (JsonProcessingException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						
						List<Tuple3<IndicatorEvent, K, IndicatorDetails>> l = optConfigMap.map(map-> convertToIndStateKeys.apply(sample.getPrimaryKey(), map))
								.map(stateGetter)
								.map(compInd)
								.map(iel->{
									return iel.stream()
										// this component only outputs alerts so 
										// filter if alert didn't trigger
										// filter where indicator didn't test
										.filter(Optional::isPresent)
										.map(Optional::get)
										// at this point we can check to see if the 
										// indicator state should be updated
										.peek(e-> {
											
											String skToUpdate = sample.getSecondaryKey();
											if(skToUpdate.equals(e.getT1().getsK())) {
												
												try {
													LoggerFactory.getLogger(SampleSubscriber.class)
														.debug("Indicator State updated: " + e.getT1().getStateKey() + " is " + om.writeValueAsString(e.getT2()));
												} catch (JsonProcessingException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												tuple3.getT3()
													.get()
													.getState()
													.put(e.getT1().getStateKey(), e.getT2());
											}
										})
										// filter where event wasn't triggered
										.filter(ie->ie.getT2().getOptEvent().isPresent())
										.map(ie-> Tuples.of( ie.getT2()
														.getOptEvent()
														.get(), 
														sample, 
														ie.getT1()))
										
										.collect(Collectors.toList());
								}) 
								.orElse(Arrays.asList());
						
						
						return l;
				
					})
					// don't need event if indicator did not produce one.
					.filter(l->l.size()>0);
			
		};
		
	}
	
	
	/**
	 * AndUpdateMatchingIndicatorState 
	 * 1-10-21 - no longer updating state in this function
	 * will bring that to the top level
	 * 
	 */
	//Function<String,
	Function<Tuple2<Double, LocalDateTime>,
	Function<List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>,
			List<Optional<Tuple2<IndicatorDetails,IndicatorTestResult>>>>> computeIndicators //AndUpdateMatchingIndicatorState
			 = (newValueAndSampleDate) -> (inds) -> { // (skToUpdate) ->
				  
				 return inds.stream().map(ind->{
					 
					 Optional<IndicatorTestSpec> testSpec = ind.getT2()
						// a previous result may be available so need to use that
						// state if it is.
						.map(tr ->
							// if a previous test result is present, check the data to see if
							// the indicator should be applied.
							Optional.of(tr)
								.filter(t ->
									 newValueAndSampleDate.getT2().isAfter(tr.getSpec().getSampleDate())
								)
								.map(t-> new IndicatorTestSpec(
				 					0, 
				 					newValueAndSampleDate.getT1(), 
				 					newValueAndSampleDate.getT2(), 
				 					tr.getSpec().getIndicatorAttributeState())))
						.orElse(
							Optional.of(new IndicatorTestSpec(
			 					0, 
			 					newValueAndSampleDate.getT1(), 
			 					newValueAndSampleDate.getT2(), 
			 					new IndicatorAttributeState()))
						);
						
					// test if present
					 Optional<Tuple2<IndicatorDetails,IndicatorTestResult>> output = testSpec.map(spec->IndicatorFactory.getIndicator(ind.getT1().getIndicatorKey())
				 				.test(spec))
					 		.map(res-> Tuples.of(ind.getT1(), res));
					 
//					 output.map(rr->rr.getT2())
//					 	.flatMap(tr-> tr.getOptEvent())
//					 	.ifPresent(a->{
//					 		LoggerFactory.getLogger(SampleSubscriber.class)
//							.info("Indicator alerted: " + e.getT1().getStateKey() + " is " + om.writeValueAsStri
//					 	});
					 
					 return output;

				 })
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
			
	Function<Supplier<MutableIndicatorStateManager>,
		Function<List<IndicatorDetails>,
			List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>>> getIndicatorFnAndState
				 = (state) -> (keys) -> keys.stream()
					.map(key-> Tuples.of(key, Optional.ofNullable(state.get().getState().get(key.getStateKey()))))
					.collect(Collectors.toList());
	
				 
}
