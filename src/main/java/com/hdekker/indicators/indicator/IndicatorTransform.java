package com.hdekker.indicators.indicator;

import java.util.Map;
import java.util.function.Function;

import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;

import reactor.util.function.Tuple2;

/**
 * Transform, must have uniuqe name within an indicator
 * so that many types of the same transform can be chained.
 * 
 * @author HDekker
 *
 */
public interface IndicatorTransform extends
			Function<Tuple2<Double, IndicatorInternalState>, Tuple2<Double, IndicatorInternalState>>{

}