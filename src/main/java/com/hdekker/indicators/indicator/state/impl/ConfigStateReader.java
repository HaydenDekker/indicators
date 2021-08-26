package com.hdekker.indicators.indicator.state.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.Indicator;

public interface ConfigStateReader extends Function<String, Optional<Map<String, List<String>>>>{

}
