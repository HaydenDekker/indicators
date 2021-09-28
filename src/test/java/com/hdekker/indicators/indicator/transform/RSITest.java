package com.hdekker.indicators.indicator.transform;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.*;

import org.junit.jupiter.api.Test;

import com.hdekker.indicators.indicator.fn.IndicatorTransform;
import com.hdekker.indicators.indicator.IndicatorFnConfigSpec;
import com.hdekker.indicators.indicator.IndicatorFnIdentity;
import com.hdekker.indicators.indicator.IndicatorFnDescriptor.IndicatorFNType;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class RSITest {
	
	@Test
	public void rsiTransformProducesCorrectValueGivenStateAndInput() {
		
		IndicatorTransform rsiIndicatorTransform = RSI.getTransform().withConfig(new IndicatorFnConfigSpec("rsi-fn-1", Map.of("steps", 14.00), IndicatorFNType.Transform, IndicatorFnIdentity.TRANSFORM_RSI));
		
		List<Double> newValue = Arrays.asList(32.5, 50.0, 52.0, 40.0, 42.0, 29.0, 28.0, 25.0, 23.0);
		IndicatorAttributeState initialState = new IndicatorAttributeState();
		
		Tuple2<Double, IndicatorAttributeState> output = rsiIndicatorTransform.apply(Tuples.of(newValue.get(0), initialState));
		assertThat(output.getT1(), closeTo(100.0, 2.0));
		// assertThat(output.getT2().getState(), hasKey("rsi-fn-1-steps"));
		assertThat(output.getT2().getState(), hasKey("rsi-fn-1-" + RSI.aveGain));
		assertThat(output.getT2().getState(), hasKey("rsi-fn-1-" + RSI.aveLoss));
		assertThat(output.getT2().getState(), hasKey("rsi-fn-1-" + RSI.rsi));
		assertThat(output.getT2().getState(), hasKey("rsi-fn-1-" + RSI.value));
		
		
	}
			
	
}
