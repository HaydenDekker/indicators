package com.hdekker.indicators.indicator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalStateManager;
import com.hdekker.indicators.indicator.state.impl.MutableInternalStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class IndicatorIT {

	@Test
	public void withMockData(){
		
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
		
		Flux<List<TestConfiguration>> testConfig = IndicatorTestDataUtil.confFluxSingleUpdate();
		
		Function<Tuple2<Flux<List<TestConfiguration>>, Flux<TestDataInputType>>, 
			Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorInternalStateManager>>, 
						Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>>> fn = IndicatorComponent.instance(Tuples.of(configState, indicatorStateMan));
		
		Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorInternalStateManager>>, Flux<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>>> out = fn.apply(Tuples.of(testConfig, inputFlux));
		
		out.getT1().subscribe();
		List<List<Tuple3<IndicatorEvent, TestDataInputType, IndicatorDetails>>> alerts = out.getT2().collect(Collectors.toList()).block();
		assertThat(alerts.get(0).get(0).getT1().getAlert(), equalTo("Value moved below set threshold 30.0"));
		
	}
	
}
