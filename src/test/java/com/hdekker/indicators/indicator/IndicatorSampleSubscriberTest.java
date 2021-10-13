package com.hdekker.indicators.indicator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.ConfigManger;
import com.hdekker.indicators.indicator.components.SampleSubscriber;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.fn.Indicator;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.state.IndicatorTestResultEvent;
import com.hdekker.indicators.indicator.state.impl.ConfigStateReader;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.state.impl.MutableIndicatorStateManager;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
		
		Flux<TestDataInputType> inputFlux = Flux.create(confSink->{
			
			TestDataInputType one = new TestDataInputType("asset-1", Duration.ofSeconds(1), LocalDateTime.now(), 4142.990459018316);
			TestDataInputType two = new TestDataInputType("asset-1", Duration.ofHours(1), LocalDateTime.now(), 4142.990459018316); // false second
			
			Mono.delay(Duration.ofSeconds(2)).subscribe(l-> confSink.next(two));
			Mono.delay(Duration.ofSeconds(3)).subscribe(l-> confSink.next(one));
			Mono.delay(Duration.ofSeconds(4)).subscribe(l-> confSink.complete());
			
			});
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		// need to initialse data
		
		IndicatorAttributeState h = new IndicatorAttributeState(Map.of(
				"1-RSI-rsi-value", 7935.522040170545,
				"1-RSI-rsi-ave-loss", 132.82257418166498,
				"1-RSI-rsi-rsi",35.19557443409366,
				"1-RSI-rsi-ave-gain",72.13653628307311,
				"2-Drops below threshold-prev-state",35.19557443409366
				));
		IndicatorTestResult state = new IndicatorTestResult(Optional.empty(),
							new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), h));
		
		MutableIndicatorStateManager indicatorStateMan = new MutableIndicatorStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
		
		ConfigStateReader r = (primaryKey) -> Optional.ofNullable(configState.getState().get(primaryKey));
		Supplier<MutableIndicatorStateManager> isr = () -> indicatorStateMan;
		
		Tuple2<Flux<IndicatorTestResultEvent>, Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>> fluxOut = SampleSubscriber.<TestDataInputType> builder().withInputs(Tuples.of(inputFlux, r, isr));
		
		// only expect event if present so needs at least two inputs
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> output = fluxOut.getT2().collect(Collectors.toList()).block();
		assertThat(output, hasSize(2));
		
	}
	
	
	@Test
	public void itBasicReadsIndicatorStateWhenSampleArrivesProducesAlert() {
	
		// data
		Flux<TestDataInputType> inputFlux = IndicatorTestDataUtil.dataFluxSingleInput();
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		MutableIndicatorStateManager indicatorStateMan = IndicatorTestDataUtil.stubMutableIndicatorStateManager();
		
		
		// function
		ConfigStateReader r = (primaryKey) -> Optional.ofNullable(configState.getState().get(primaryKey));
		Supplier<MutableIndicatorStateManager> isr = () -> indicatorStateMan;
		
		Tuple2<Flux<IndicatorTestResultEvent>,Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>>
			fluxOut = SampleSubscriber.<TestDataInputType> builder()
						.withInputs(Tuples.of(inputFlux, r, isr));
		
		// only expect event if present so needs at least two inputs
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> output = fluxOut.getT2().collect(Collectors.toList()).block();
		assertThat(output, hasSize(1));
		
	}
	
	// units
	@Test
	public void canObtainIndicatorsForSample() {
		
		List<TestDataInputType> testData = IndicatorTestDataUtil.getTestData();
		
		// just a read of state.
		// borrowed from config man tests
		List<IndicatorSubscription> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		List<Tuple3<String, String, String>> newItems = 
															ConfigManger.convertForFiltering
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
		List<IndicatorSubscription> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		List<Tuple3<String, String, String>> newItems = 
											ConfigManger.convertForFiltering
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
		IndicatorAttributeState h = new IndicatorAttributeState(Map.of(
				"test", 0.01
				));
		IndicatorTestResult state = new IndicatorTestResult(Optional.empty(),
				new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), h));

		MutableIndicatorStateManager iism = new MutableIndicatorStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
		
		// comp interface
		Supplier<MutableIndicatorStateManager> isr = () -> iism;
		
		// what subscriber sees
		Map<String, Double> internalIndState = isr.get().getState().get(keys.get(0).getStateKey()).getSpec().getIndicatorAttributeState().getState();
		assertThat(internalIndState.get("test"), equalTo(0.01));
		
	}
	
	@BeforeEach
	public void setIndicator() {
		
		IndicatorTestDataUtil.indicatorTestConfigurations()
		.subscribe(con-> con.forEach(c-> IndicatorFactory.configureIndicator(c)));

		assertThat(IndicatorFactory.configuredIndicators, notNullValue());
		
	}
	
	/**
	 *  Only a single indicator is configured
	 *  To test this I need to mock additional indicators
	 * 
	 */
	
//	@Test 
//	public void uMapsSampleToIndicatorAlertsForAListOfIndicators() {
//		
//		// data
//		List<TestDataInputType> testData = IndicatorTestDataUtil.getTestData();	
//		
//		IndicatorAttributeState h = new IndicatorAttributeState(Map.of(
//				"1-RSI-rsi-value", 7935.522040170545,
//				"1-RSI-rsi-ave-loss", 132.82257418166498,
//				"1-RSI-rsi-rsi",35.19557443409366,
//				"1-RSI-rsi-ave-gain",72.13653628307311,
//				"2-Drops below threshold-prev-state",35.19557443409366
//				));
//		IndicatorTestResult state = new IndicatorTestResult(Optional.empty(),
//				new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), h));
//		
//		Map<String, IndicatorTestResult> statem = Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state);
//		MutableIndicatorStateManager iism = new MutableIndicatorStateManager(statem);
//		
//		IndicatorTestResult intState = iism.getState().get("asset-1-PT1S-RSI-14-ThreshBelow30");
//		
//		// function
//		Indicator i = IndicatorFactory.getIndicator(IndicatorTestDataUtil.RSI14_THRESH_BELOW30);
//		
//		// apply
//		IndicatorTestResult out = i.test(new IndicatorTestSpec(0, testData.get(0).getValue(), LocalDateTime.now(), intState.getSpec().getIndicatorAttributeState()));
//		
//		// assess
//		assertThat(out.getOptEvent().isEmpty(), equalTo(true));
//		
//	}
	
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
		
		// Stub state
		MutableIndicatorStateManager indicatorStateMan = IndicatorTestDataUtil.stubMutableIndicatorStateManager();
		
		// function
		Function<Supplier<MutableIndicatorStateManager>, 
			Function<List<IndicatorDetails>, 
			List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>>> 
			fn = SampleSubscriber.getIndicatorFnAndState;
		
		Function<List<IndicatorDetails>, 
			List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>> 
			indCompFn = fn.apply(()->indicatorStateMan);
		
		// apply fn
		List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>> out = indCompFn.apply(
							Arrays.asList(new IndicatorDetails("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", "RSI-14-ThreshBelow30", "PT1S")));
		assertThat(out.get(0).getT2().get(), equalTo(indicatorStateMan.getState().get("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30")));
		
	}
	
	// data from BTC and indicator Factory test
	//{"rsi-fn-1-rsi-value":7935.522040170545,"rsi-fn-1-rsi-ave-loss":132.82257418166498,"rsi-fn-1-rsi-rsi":35.19557443409366,"thresh-alt-prev-state":35.19557443409366,"rsi-fn-1-rsi-ave-gain":72.13653628307311}
	//{"rsi-fn-1-rsi-value":5142.990459018316,"rsi-fn-1-rsi-ave-loss":322.80178896527667,"rsi-fn-1-rsi-rsi":17.184807929727413,"thresh-alt-prev-state":17.184807929727413,"rsi-fn-1-rsi-ave-gain":66.9839265485679}
	
	
	@Test
	public void uComputeIndicatorsAndUpdateMatchingIndicatorState() {
		
		// stub state
		IndicatorAttributeState h = new IndicatorAttributeState(Map.of(
				"1-RSI-rsi-value", 7935.522040170545,
				"1-RSI-rsi-ave-loss", 132.82257418166498,
				"1-RSI-rsi-rsi",35.19557443409366,
				"1-RSI-rsi-ave-gain",72.13653628307311,
				"2-Drops below threshold-prev-state",35.19557443409366
				));
		IndicatorTestResult previousResult = new IndicatorTestResult(Optional.empty(),
				new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), h));
		
		IndicatorDetails inddesc = new IndicatorDetails("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", "RSI-14-ThreshBelow30", "PT1S");
		List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>> existingState = Arrays.asList(Tuples.of(inddesc, Optional.of(previousResult)));
		
		// mock fun
		Function<Tuple2<Double, LocalDateTime>, 
			Function<List<Tuple2<IndicatorDetails, Optional<IndicatorTestResult>>>, 
			List<Optional<Tuple2<IndicatorDetails, IndicatorTestResult>>>>> 
			fn = SampleSubscriber.computeIndicators;
	
		
		List<Optional<Tuple2<IndicatorDetails, IndicatorTestResult>>> out = fn.apply(Tuples.of(5142.990459018316, LocalDateTime.now()))
																.apply(existingState);
		assertThat(out.get(0).get().getT2().getOptEvent().get().getAlert(), equalTo("Value moved below set threshold 30.0"));
		assertThat(out.get(0).get().getT1().getStateKey(), equalTo("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30"));
	}
}
