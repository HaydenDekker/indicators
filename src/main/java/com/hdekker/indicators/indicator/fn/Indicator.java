package com.hdekker.indicators.indicator.fn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
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
	
		IndicatorTestResult test(
				IndicatorTestSpec indicatorTestSpec);
	
	
	/**
	 * Holds data for an indicator test.
	 * @author HDekker
	 *
	 */
	public class IndicatorTestSpec{
		
		final Integer inputPathNum;
		final Double value;
		final LocalDateTime sampleDate;
		final IndicatorAttributeState indicatorAttributeState;
		
		public IndicatorTestSpec(Integer inputPathNum, Double value, LocalDateTime sampleDate, IndicatorAttributeState indicatorAttributeState) {
			super();
			this.inputPathNum = inputPathNum;
			this.value = value;
			this.indicatorAttributeState = indicatorAttributeState;
			this.sampleDate = sampleDate;
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

		public LocalDateTime getSampleDate() {
			return sampleDate;
		}

	}
	
	public class IndicatorTestResult {
		
		final Optional<IndicatorEvent> optEvent;
		//final IndicatorDetails details;
		final IndicatorTestSpec spec;
		
		public IndicatorTestResult(Optional<IndicatorEvent> optEvent,
				IndicatorTestSpec spec) {
			super();
			this.optEvent = optEvent;
			//this.details = details;
			this.spec = spec;
		}
		public Optional<IndicatorEvent> getOptEvent() {
			return optEvent;
		}
		
		public IndicatorTestSpec getSpec() {
			return spec;
		}
		
		
		
	}
}
