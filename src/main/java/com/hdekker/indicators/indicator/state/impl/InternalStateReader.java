package com.hdekker.indicators.indicator.state.impl;

import java.util.function.Function;

import reactor.util.function.Tuple2;

public interface InternalStateReader extends  Function<String, Tuple2<MutableInternalStateHolder, Integer>>{

}
