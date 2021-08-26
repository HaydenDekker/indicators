package com.hdekker.indicators.indicator;

import java.util.Optional;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

/**
 * All indicators are stateless,
 * therefore the state is passed in and out
 * for every calculation, by way of Map<String, Double>
 * 
 * @author HDekker
 *
 */
public interface Indicator {
	Tuple2<
		Optional<IndicatorEvent>,
		IndicatorInternalState> test(
				Tuple3<Integer, Double, IndicatorInternalState
				> input
	);
}
