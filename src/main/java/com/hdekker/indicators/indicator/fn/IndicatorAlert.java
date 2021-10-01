package com.hdekker.indicators.indicator.fn;

import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;

import reactor.util.function.Tuple2;

public interface IndicatorAlert extends 
				Function<IndicatorTestSpec, 
					Tuple2<Optional<IndicatorEvent>, IndicatorTestSpec>>{
	
}
