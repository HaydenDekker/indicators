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
import com.hdekker.indicators.indicator.state.IndicatorTestResultEvent;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.MutableIndicatorStateManager;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class IndicatorIT {

	@Test
	public void withMockData(){
		
		Flux<TestDataInputType> inputFlux = IndicatorTestDataUtil.dataFluxSingleInput();
		
		IndicatorConfigState configState = new IndicatorConfigState(Map.of("asset-1", Map.of("PT1S", List.of("RSI-14-ThreshBelow30"))));
		
		MutableIndicatorStateManager indicatorStateMan = IndicatorTestDataUtil.stubMutableIndicatorStateManager();
		
		Flux<List<IndicatorSubscription>> testConfig = IndicatorTestDataUtil.confFluxSingleUpdate();
	
		IndicatorComponentInputSpec<TestDataInputType> 
			icis = new IndicatorComponentInputSpec<TestDataInputType>(testConfig, null, inputFlux);
		
		// build component
		Function<IndicatorComponentInputSpec<TestDataInputType>, 
			Tuple3<Flux<Tuple2<IndicatorConfigState, MutableIndicatorStateManager>>, 
			Flux<IndicatorTestResultEvent>,Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>>> 
				fn = IndicatorComponent.instance(Tuples.of(configState, indicatorStateMan));
		
		// apply component
		Tuple3<Flux<Tuple2<IndicatorConfigState, MutableIndicatorStateManager>>,
			Flux<IndicatorTestResultEvent>,
		 Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>> 
			out = fn.apply(icis);
		
		out.getT1().subscribe();
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> alerts = out.getT3().collect(Collectors.toList()).block();
		assertThat(alerts.get(0).get(0).getT1().getAlert(), equalTo("Value moved below set threshold 30.0"));
		
	}
	
}
