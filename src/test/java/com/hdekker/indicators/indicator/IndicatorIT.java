package com.hdekker.indicators.indicator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.IndicatorComponent.IndicatorComponentInputSpec;
import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.state.impl.IndicatorStateManager;
import com.hdekker.indicators.indicator.state.impl.MutableAttributeStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class IndicatorIT {

	@Test
	public void withMockData(){
		
		Flux<TestDataInputType> inputFlux = IndicatorTestDataUtil.dataFluxSingleInput();
		
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		
		MutableAttributeStateHolder h = new MutableAttributeStateHolder();
		h.setState(new IndicatorAttributeState(Map.of(
				"rsi-fn-1-rsi-value", 7935.522040170545,
				"rsi-fn-1-rsi-ave-loss", 132.82257418166498,
				"rsi-fn-1-rsi-ave-gain",72.13653628307311,
				"thresh-alt-prev-state",35.19557443409366
				)));
				
		Tuple2<MutableAttributeStateHolder, Integer> state = Tuples.of(h, 0);
		IndicatorStateManager indicatorStateMan = new IndicatorStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
		
		Flux<List<IndicatorSubscription>> testConfig = IndicatorTestDataUtil.confFluxSingleUpdate();
	
		IndicatorComponentInputSpec<TestDataInputType> 
			icis = new IndicatorComponentInputSpec<TestDataInputType>(testConfig, null, inputFlux);
		
		// build component
		Function<IndicatorComponentInputSpec<TestDataInputType>, 
			Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorStateManager>>, 
				Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>>> 
				fn = IndicatorComponent.instance(Tuples.of(configState, indicatorStateMan));
		
		// apply component
		Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorStateManager>>, 
		 Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>> 
			out = fn.apply(icis);
		
		out.getT1().subscribe();
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> alerts = out.getT2().collect(Collectors.toList()).block();
		assertThat(alerts.get(0).get(0).getT1().getAlert(), equalTo("Value moved below set threshold 30.0"));
		
	}
	
}
