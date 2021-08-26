package com.hdekker.indicators.indicator.transform;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.*;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.IndicatorTransform;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class RSITest {
	
	@Test
	public void rsiTransformProducesCorrectValueGivenStateAndInput() {
		
		IndicatorInternalState conf = IndicatorInternalState.builder("rsi-fn-1")
					.put("steps", 14.00)
					.build();
		
		IndicatorTransform rsiIndicatorTransform = RSI.getTransform().withConfig(conf);
		
		List<Double> newValue = Arrays.asList(32.5, 50.0, 52.0, 40.0, 42.0, 29.0, 28.0, 25.0, 23.0);
		IndicatorInternalState initialState = IndicatorInternalState.builder("rsi-fn-1")
							.build();
		
		Tuple2<Double, IndicatorInternalState> output = rsiIndicatorTransform.apply(Tuples.of(newValue.get(0), initialState));
		assertThat(output.getT1(), closeTo(100.0, 2.0));
		// assertThat(output.getT2().getState(), hasKey("rsi-fn-1-steps"));
		assertThat(output.getT2().getState(), hasKey("rsi-fn-1-rsi-ave-gain"));
		
	}
			
	
}
