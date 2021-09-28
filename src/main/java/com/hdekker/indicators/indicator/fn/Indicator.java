package com.hdekker.indicators.indicator.fn;

import java.util.Optional;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

import reactor.util.function.Tuple2;

/**
 * All indicators are stateless,
 * therefore the state is passed in and out
 * for every calculation, by way of Map<String, Double>
 * 
 * You test new data against the indicator to 
 * see if an event is produced.
 * 
 * @author HDekker
 *
 */
public interface Indicator {
	Tuple2<
		Optional<IndicatorEvent>,
		IndicatorAttributeState> test(
				IndicatorTestSpec indicatorTestSpec);
	
	
	/**
	 * Holds data for an indicator test.
	 * @author HDekker
	 *
	 */
	public class IndicatorTestSpec{
		
		final Integer inputPathNum;
		final Double value;
		final IndicatorAttributeState indicatorAttributeState;
		
		public IndicatorTestSpec(Integer inputPathNum, Double value, IndicatorAttributeState indicatorAttributeState) {
			super();
			this.inputPathNum = inputPathNum;
			this.value = value;
			this.indicatorAttributeState = indicatorAttributeState;
		}

		public Integer getInputPathNum() {
			return inputPathNum;
		}

		public Double getValue() {
			return value;
		}

		public IndicatorAttributeState getIndicatorAttributeState() {
			return indicatorAttributeState;
		}

	}
}
