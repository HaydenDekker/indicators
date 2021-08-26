package com.hdekker.indicators.indicator.alert;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.hdekker.indicators.indicator.IndicatorAlert;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalStateManager;

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
				.withConfig(IndicatorInternalState.builder("isd23")
																	.put("threshold", 10.00).build()
		);
		long dur1 = System.currentTimeMillis();
		Tuple2<Optional<IndicatorEvent>, IndicatorInternalState> alert = fn.apply(
																	Tuples.of(11.00, 
																		IndicatorInternalState.builder("isd23")
																			.put(Threshold.PREV_STATE, 7.00)
																			.build()));
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
		
		IndicatorAlert ia = Threshold.movesBelowThresholdAlert()
						.withConfig(IndicatorInternalState.builder("isd23").put("threshold", 10.00).build());
		
		IndicatorInternalState prevState = IndicatorInternalState.builder("isd23").put("prev-state", 7.00).build();
		Tuple2<Optional<IndicatorEvent>, IndicatorInternalState> empty = ia.apply(Tuples.of(11.00, prevState));
		assertThat(empty.getT1(), equalTo(Optional.empty()));
		Tuple2<Optional<IndicatorEvent>, IndicatorInternalState> alert = ia.apply(Tuples.of(7.00,empty.getT2()));
		assertThat(alert.getT1().get().getAlert(), equalTo("Value moved below set threshold 10.0"));
		
	}
	
}
