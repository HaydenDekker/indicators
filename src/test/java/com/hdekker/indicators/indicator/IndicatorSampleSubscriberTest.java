package com.hdekker.indicators.indicator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.ConfigManger;
import com.hdekker.indicators.indicator.components.SampleSubscriber;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.state.impl.ConfigStateReader;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalStateManager;
import com.hdekker.indicators.indicator.state.impl.InternalStateReader;
import com.hdekker.indicators.indicator.state.impl.MutableInternalStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class IndicatorSampleSubscriberTest {

	@Test
	public void withDataTypeWhereNoConfigCanBeFoundForPK() {
		
	}
	
	/**
	 * With only config for a signle sk,
	 * sample should still be tested agains confi for matching pks.
	 * 
	 * In this case an alert should be produced twice. Once for the sample with matching conf against matchning state
	 * and the second for the sample with no matching conf againt the only PK state
	 * 
	 * The second alert needs to identify that the non-matching sample would have
	 * triggered the alert
	 * 
	 */
	@Test
	public void itWithDataTypeWhereNoConfigCanBeFoundForSK() {
		
		Flux<TestDataInputType> inputFlux = IndicatorTestDataUtil.dataFluxDualInputUniqueSKs();
		
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		// need to initialse mock data
		
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("rsi-fn-1")
				.put("rsi-value", 7935.522040170545)
				.put("rsi-ave-loss", 132.82257418166498)
				.put("rsi-rsi",35.19557443409366)
				.put("rsi-ave-gain",72.13653628307311)
				.build()
				.bindTo("thresh-alt")
				.builder()
				.put("prev-state",35.19557443409366)
				.build());
		Tuple2<MutableInternalStateHolder, Integer> state = Tuples.of(h, 0);
		IndicatorInternalStateManager indicatorStateMan = new IndicatorInternalStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
		
		// need to initialse mock data
		
		ConfigStateReader r = (primaryKey) -> Optional.ofNullable(configState.getState().get(primaryKey));
		InternalStateReader isr = (s) -> indicatorStateMan.getState().get(s);
		
		Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> fluxOut = SampleSubscriber.<TestDataInputType> builder().withInputs(Tuples.of(inputFlux, r, isr));
		
		// only expect event if present so needs at least two inputs
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> output = fluxOut.collect(Collectors.toList()).block();
		assertThat(output, hasSize(2));
		
	}
	
	
	@Test
	public void itBasicReadsIndicatorStateWhenSampleArrivesProducesAlert() {
	
		Flux<TestDataInputType> inputFlux = IndicatorTestDataUtil.dataFluxSingleInput();
		
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		// need to initialse mock data
		
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("rsi-fn-1")
				.put("rsi-value", 7935.522040170545)
				.put("rsi-ave-loss", 132.82257418166498)
				.put("rsi-rsi",35.19557443409366)
				.put("rsi-ave-gain",72.13653628307311)
				.build()
				.bindTo("thresh-alt")
				.builder()
				.put("prev-state",35.19557443409366)
				.build());
		Tuple2<MutableInternalStateHolder, Integer> state = Tuples.of(h, 0);
		IndicatorInternalStateManager indicatorStateMan = new IndicatorInternalStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
		
		// need to initialse mock data
		
		ConfigStateReader r = (primaryKey) -> Optional.ofNullable(configState.getState().get(primaryKey));
		InternalStateReader isr = (s) -> indicatorStateMan.getState().get(s);
		
		Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> fluxOut = SampleSubscriber.<TestDataInputType> builder().withInputs(Tuples.of(inputFlux, r, isr));
		
		// only expect event if present so needs at least two inputs
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> output = fluxOut.collect(Collectors.toList()).block();
		assertThat(output, hasSize(1));
		
		
	}
	
	// units
	@Test
	public void canObtainIndicatorsForSample() {
		
		List<TestDataInputType> testData = IndicatorTestDataUtil.getTestData();
		
		// just a read of state.
		// borrowed from config man tests
		List<TestConfiguration> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		List<Tuple3<String, String, String>> newItems = 
															ConfigManger.inputConversionFn1
															.andThen(ConfigManger.flattenState)
															.apply(initial);
		
		Map<String, Map<String, List<String>>> newState = ConfigManger.buildState.apply(newItems);
		
		// indicators for sample
		List<String> indicators = newState.get(testData.get(0).getPrimaryKey())
				.get(testData.get(0).getSecondaryKey());
		
		assertThat(indicators, contains(IndicatorTestDataUtil.RSI14_THRESH_BELOW30));
		
		
	}
	

	@Test
	public void canObtainIndicatorInternalStateForAllIndicators() {
		
		// just two state reads.
		List<TestDataInputType> testData = IndicatorTestDataUtil.getTestData();
		
		// just a read of state.
		// borrowed from config man tests
		List<TestConfiguration> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		List<Tuple3<String, String, String>> newItems = 
											ConfigManger.inputConversionFn1
											.andThen(ConfigManger.flattenState)
											.apply(initial);
		
		Map<String, Map<String, List<String>>> newState = ConfigManger.buildState.apply(newItems);
		
		// indicators for sample
		List<String> indicators = newState.get(testData.get(0).getPrimaryKey())
				.get(testData.get(0).getSecondaryKey());
		
		// subscriber fns
		List<IndicatorDetails> keys = SampleSubscriber.getIndicatorStateKeys.apply(Tuples.of(testData.get(0).getPrimaryKey(), 
											testData.get(0).getSecondaryKey(), indicators));
		
		assertThat(keys.get(0).getStateKey(), equalTo("asset-1-PT1S-RSI-14-ThreshBelow30"));
		
		// Mocks input state
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("").put("test", "isCorrect").build());
		Map<String, Tuple2<MutableInternalStateHolder, Integer>> state = Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", Tuples.of(h, 0));
		IndicatorInternalStateManager iism = new IndicatorInternalStateManager(state);
		
		// comp interface
		InternalStateReader reader = (s) -> iism.getState().get(s);
		
		// what subscriber sees
		Map<String, Object> internalIndState = reader.apply(keys.get(0).getStateKey()).getT1().getState().getState();
		assertThat(internalIndState.get("-test"), equalTo("isCorrect"));
		
	}
	@Test 
	public void mapsSampleToIndicatorAlertsForAListOfIndicators() {
		
		List<TestDataInputType> testData = IndicatorTestDataUtil.getTestData();	
		Indicator i = IndicatoryFactory.getIndicator("RSI-14-ThreshBelow30");
		
		// Mocks input state reader
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("").build());
		Map<String, Tuple2<MutableInternalStateHolder, Integer>> state = Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", Tuples.of(h, 0));
		IndicatorInternalStateManager iism = new IndicatorInternalStateManager(state);
		// comp interface
		InternalStateReader reader = (s) -> iism.getState().get(s);
		
		Tuple2<MutableInternalStateHolder, Integer> intState = reader.apply("asset-1-PT1S-RSI-14-ThreshBelow30");
		
		Tuple3<Integer, Double, IndicatorInternalState> input = Tuples.of(0, testData.get(0).getValue(), intState.getT1().getState());
		Tuple2<Optional<IndicatorEvent>, IndicatorInternalState> out = i.test(input);
		intState.getT1().setState(out.getT2());
		
		assertThat(out.getT1().isEmpty(), equalTo(true));
		
	}
	
	@Test
	public void updatesStateForMatchingSKIndicators() {
		
		MutableInternalStateHolder mish = new MutableInternalStateHolder();
		mish.setState(IndicatorInternalState.builder("test").build());
		assertThat(mish.getState().fnId, equalTo("test"));
		
		
	}
	
	@Test
	public void uConvertToIndStateKeys() {
		
		BiFunction<String, Map<String, List<String>>, List<IndicatorDetails>> fn = SampleSubscriber.convertToIndStateKeys;
		Map<String, List<String>> sks = Map.of("skey2", Arrays.asList("ind1"));
		String pk = "pk1";
		List<IndicatorDetails> out = fn.apply(pk, sks);
		assertThat(out.get(0).getStateKey(), equalTo("pk1-skey2-ind1"));
		assertThat(out.get(0).getIndicatorKey(), equalTo("ind1"));
		
		
	}
	
	@Test
	public void uGetIndicatorFnAndState() {
		
		Function<InternalStateReader, 
		Function<List<IndicatorDetails>, 
			List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>>>> fn = SampleSubscriber.getIndicatorFnAndState;
		
		// Mocks input state reader
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("").build());
		Map<String, Tuple2<MutableInternalStateHolder, Integer>> state = Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", Tuples.of(h, 0));
		IndicatorInternalStateManager iism = new IndicatorInternalStateManager(state);
		// comp interface
		InternalStateReader reader = (s) -> iism.getState().get(s);
		
		Function<List<IndicatorDetails>, List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>>> indCompFn = fn.apply(reader);
		List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>> out = indCompFn.apply(Arrays.asList(new IndicatorDetails("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", "RSI-14-ThreshBelow30", "PT1S")));
		assertThat(out.get(0).getT2().getT1(), equalTo(h));
		
	}
	
	// data from BTC and indicator Factory test
	//{"rsi-fn-1-rsi-value":7935.522040170545,"rsi-fn-1-rsi-ave-loss":132.82257418166498,"rsi-fn-1-rsi-rsi":35.19557443409366,"thresh-alt-prev-state":35.19557443409366,"rsi-fn-1-rsi-ave-gain":72.13653628307311}
	//{"rsi-fn-1-rsi-value":5142.990459018316,"rsi-fn-1-rsi-ave-loss":322.80178896527667,"rsi-fn-1-rsi-rsi":17.184807929727413,"thresh-alt-prev-state":17.184807929727413,"rsi-fn-1-rsi-ave-gain":66.9839265485679}
	
	
	@Test
	public void uComputeIndicatorsAndUpdateMatchingIndicatorState() {
		
		Function<String, Function<Double, Function<List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>>, List<Tuple2<IndicatorEvent, IndicatorDetails>>>>> fn = SampleSubscriber.computeIndicatorsAndUpdateMatchingIndicatorState;
		
		Function<Double, Function<List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>>, List<Tuple2<IndicatorEvent, IndicatorDetails>>>> compfn = fn.apply("asset-1");
		
		// state
		MutableInternalStateHolder h = new MutableInternalStateHolder();
		h.setState(IndicatorInternalState.builder("rsi-fn-1")
				.put("rsi-value", 7935.522040170545)
				.put("rsi-ave-loss", 132.82257418166498)
				.put("rsi-rsi",35.19557443409366)
				.put("rsi-ave-gain",72.13653628307311)
				.build()
				.bindTo("thresh-alt")
				.builder()
				.put("prev-state",35.19557443409366)
				.build());
		Tuple2<MutableInternalStateHolder, Integer> state = Tuples.of(h, 0);
		IndicatorDetails inddesc = new IndicatorDetails("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", "RSI-14-ThreshBelow30", "PT1S");
		List<Tuple2<IndicatorDetails, Tuple2<MutableInternalStateHolder, Integer>>> existingState = Arrays.asList(Tuples.of(inddesc, state));
		List<Tuple2<IndicatorEvent, IndicatorDetails>> out = compfn.apply(5142.990459018316).apply(existingState);
		assertThat(out.get(0).getT1().getAlert(), equalTo("Value moved below set threshold 30.0"));
		assertThat(out.get(0).getT2().getStateKey(), equalTo("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30"));
	}
}
