package com.hdekker.indicators.indicator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.components.ConfigManger;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorStateManager;
import com.hdekker.indicators.indicator.state.impl.MutableAttributeStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Indicator Config's IC's | dynamic | flexible
 * 
 * User specifies data using a primary key PK
 * and a secondary key SK
 * User specifies indicator ID
 * 
 * User can change conf at any time.
 * Indicators are stateful, their value depends 
 * on their past values, this manages Ind state.
 * 
 * If conf changes, ind state of existing conf 
 * is maintained.
 * 
 * @author HDekker
 *
 */
public class IndicatorConfigManagerTest {
	
	IndicatorConfigState icsComponentReference;
	IndicatorStateManager iismComponentRef;
	
	@Test
	public void itConfigManagerProducesIndicatorConfigurationMapForASignleConfigUpdate() {
		
		// state type information hidden
		icsComponentReference = IndicatorComponent.getIndicatorConfigManagerInstance();
		iismComponentRef = IndicatorComponent.getIndicatorStateInstance();
		
		ConfigManger<TestConfiguration> fn = ConfigManger.buildStandardConfMan();
		
		Flux<List<TestConfiguration>> testConfig = IndicatorTestDataUtil.confFluxSingleUpdate();
		
		Flux<Tuple2<IndicatorConfigState, IndicatorStateManager>> output = fn.withInputs(Tuples.of(testConfig, ()-> icsComponentReference, (cf) -> {icsComponentReference = cf;}, ()-> iismComponentRef, (is)->{ iismComponentRef = is;}));
		List<Tuple2<IndicatorConfigState, IndicatorStateManager>> result = output.collect(Collectors.toList()).block();
		
		assertThat(result, hasSize(1)); // success result should output exactly one for this test.
		assertThat(iismComponentRef.getState().get("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30"), notNullValue());
		
	}
	
	/**
	 * Idea of this setup is to maintain concurrency accross this
	 * asyn config and a consumer async task. So consumer needs to
	 * store updates against items and they need to make their 
	 * way into the new state if the config remains.
	 * 
	 */
	@Test
	public void itConfigManagerUpdatesConfAsInternalStateUpdatedExternallyAndFinalStateReflectsUpdateInternalState() {
		
	}
	
	Function<TestConfiguration, Tuple3<String, String, List<String>>> inputTrans  = (in) -> Tuples.of(in.getAssetPrimaryKey(), in.getAssetSecondaryKey(), in.getIndicatorId());
	
	<T, K> Function<List<T>, List<K>> listConvert(Function<T, K> conv){
		return (in) -> in.stream().map(conv).collect(Collectors.toList());
	}
	
	// units
	@Test
	public void uFindsExistingConfig() {
		
		
		// don't need reactive stage
		List<TestConfiguration> initial = IndicatorTestDataUtil.getSingleTestConfiguration();
		List<TestConfiguration> last = IndicatorTestDataUtil.getMultiTestConfiguration();
		
		IndicatorConfigState ics = new IndicatorConfigState(ConfigManger.inputConversionFn1.apply(initial));

		// new conf comes in
		List<Tuple3<String, String, String>> existingConfig = ConfigManger.findExistingConfig.apply(ConfigManger.inputConversionFn1.apply(last), ics.getState());		
	
		assertThat(existingConfig.size(), equalTo(1));
		assertThat(existingConfig.get(0).getT1(), equalTo("asset-1"));
	
	}
	
	@Test
	public void uBuildsNewState() {
		
		List<TestConfiguration> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		
		List<Tuple3<String, String, String>> newItems = 
															ConfigManger.inputConversionFn1
															.andThen(ConfigManger.flattenState)
															.apply(initial);
		
		Map<String, Map<String, List<String>>> newState = ConfigManger.buildState.apply(newItems);
		
		assertThat(newState.get("asset-1"), notNullValue());
		assertThat(newState.get("asset-2").keySet(), hasSize(2));
		
		
	}

	/**
	 * Config needs to create new maps in order to free previous
	 * states.
	 * 
	 */
	@Test
	public void uCreatesNewIndicatorState() {
		
		List<TestConfiguration> initial = IndicatorTestDataUtil.getMultiTestConfWithMultiIndicators();
		
		List<Tuple3<String, String, String>> newItems = 
															ConfigManger.inputConversionFn1
															.andThen(ConfigManger.flattenState)
															.apply(initial);
		
		Map<String, Tuple2<MutableAttributeStateHolder, Integer>> map = IndicatorStateManager.getInternalStateMap.apply(IndicatorStateManager.withNewInternalState)
																					.apply(newItems);
		
		IndicatorStateManager manager = new IndicatorStateManager(map);
		
		assertThat(manager.getState().get("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30"), notNullValue());
		
	}

}
