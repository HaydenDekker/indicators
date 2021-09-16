package com.hdekker.indicators.indicator;

import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

import reactor.util.function.Tuple2;

public interface IndicatorAlert extends 
				Function<Tuple2<Double, IndicatorAttributeState>, Tuple2<Optional<IndicatorEvent>, IndicatorAttributeState>>{
	
}
