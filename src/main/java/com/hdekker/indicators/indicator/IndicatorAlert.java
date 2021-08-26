package com.hdekker.indicators.indicator;

import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuple2;

public interface IndicatorAlert extends 
				Function<Tuple2<Double, IndicatorInternalState>, Tuple2<Optional<IndicatorEvent>, IndicatorInternalState>>{
	
}
