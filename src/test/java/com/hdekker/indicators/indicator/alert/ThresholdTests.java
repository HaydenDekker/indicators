package com.hdekker.indicators.indicator.alert;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.fn.IndicatorAlert;
import com.hdekker.indicators.indicator.IndicatorFnConfigSpec;
import com.hdekker.indicators.indicator.IndicatorFnIdentity;
import com.hdekker.indicators.indicator.IndicatorFnDescriptor.IndicatorFNType;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ThresholdTests {

	Function<Double, BiPredicate<Double, Double>> fn = Threshold.movedAboveThreshold;
	
	@Test
	public void thresholdTesterMovesAboveWhenBelow() {
		
		Double threshold = 10.00;
		Double initial = 7.00;
		Double current = 11.00;
		BiPredicate<Double, Double> detector = fn.apply(threshold);
		boolean output = detector.test(current, initial);
		assertThat(Boolean.valueOf(output), equalTo(true));
		
	}
	
	@Test
	public void threshIsNotPassed() {
		
		Double threshold = 12.00;
		Double initial = 7.00;
		Double current = 11.00;
		BiPredicate<Double, Double> detector = fn.apply(threshold);
		boolean output = detector.test(current, initial);
		assertThat(Boolean.valueOf(output), equalTo(false));
		
	}
	
	@Test
	public void alertsToThresholdExceed() {
		
		IndicatorAlert fn = Threshold.movesAboveThresholdAlert()
				.withConfig(new IndicatorFnConfigSpec("isd23",Map.of("value", 10.00), IndicatorFNType.Alert, IndicatorFnIdentity.ALERT_THRESHOLD_ABOVE));
		
		long dur1 = System.currentTimeMillis();
		Tuple2<Optional<IndicatorEvent>, IndicatorTestSpec> alert = fn.apply(
																	new IndicatorTestSpec(0, 11.00, LocalDateTime.now(), 
																			new IndicatorAttributeState(Map.of("isd23-"+Threshold.PREV_STATE, 7.00))));
		long dur2 = System.currentTimeMillis();
		long exetime = (dur2 - dur1);
		LoggerFactory.getLogger(ThresholdTests.class)
						.info(
							"Threshold evalutation took " + exetime + " millis to complete. Allowing"
							+ "a maximum of " + 1000/exetime + " samples per second."
						);
		assertThat(alert.getT1().get().getAlert(), equalTo("Value moved above set threshold 10.0"));

	}
	
	@Test
	public void alertsToThesholdSubceed() {
		
		IndicatorAlert fn = Threshold.movesBelowThresholdAlert()
				.withConfig(new IndicatorFnConfigSpec("isd23", Map.of("value", 10.00), IndicatorFNType.Alert, IndicatorFnIdentity.ALERT_THRESHOLD_BELOW));
		
		IndicatorAttributeState prevState = new IndicatorAttributeState(Map.of("isd23-"+Threshold.PREV_STATE, 7.00));
		Tuple2<Optional<IndicatorEvent>, IndicatorTestSpec> empty = fn.apply(new IndicatorTestSpec(0, 11.00, LocalDateTime.now(), prevState));
		assertThat(empty.getT1(), equalTo(Optional.empty()));
		Tuple2<Optional<IndicatorEvent>, IndicatorTestSpec> alert = fn.apply(new IndicatorTestSpec(0, 7.00, LocalDateTime.now(),empty.getT2().getIndicatorAttributeState()));
		assertThat(alert.getT1().get().getAlert(), equalTo("Value moved below set threshold 10.0"));
		
	}
	
}
