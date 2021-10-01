package com.hdekker.indicators.indicator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hdekker.indicators.indicator.IndicatorFnDescriptor.IndicatorFNType;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.state.impl.MutableIndicatorStateManager;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IndicatorTestDataUtil {

	public static final String RSI14_THRESH_BELOW30 = "RSI-14-ThreshBelow30";

	// TODO as I write transforms and indicators best to create 
		// test data that can trigger those and pass back to a 
		// central method to push through this integration test.
		public static List<TestDataInputType> getTestData(){
			
			TestDataInputType one = new TestDataInputType("asset-1", Duration.ofSeconds(1), LocalDateTime.now(), 5142.990459018316);	
			return Arrays.asList(one);
		}
		
		public static List<IndicatorSubscription> getSingleTestConfiguration(){
			
			IndicatorSubscription one = new IndicatorSubscription("asset-1", Duration.ofSeconds(1).toString(), RSI14_THRESH_BELOW30);
			return Arrays.asList(one);
		}
		
		public static List<IndicatorSubscription> getMultiTestConfiguration(){
			
			TestConfiguration one = new TestConfiguration("asset-1", Duration.ofSeconds(1), RSI14_THRESH_BELOW30);
			TestConfiguration two = new TestConfiguration("asset-2", Duration.ofSeconds(1), RSI14_THRESH_BELOW30);
			
			return Arrays.asList(one, two);
		}
		
		public static List<IndicatorSubscription> getMultiTestConfWithMultiIndicators(){
			
			IndicatorSubscription one = new IndicatorSubscription("asset-1", Duration.ofSeconds(1).toString(), RSI14_THRESH_BELOW30);
			IndicatorSubscription two = new IndicatorSubscription("asset-2", Duration.ofSeconds(1).toString(), RSI14_THRESH_BELOW30);
			IndicatorSubscription three = new IndicatorSubscription("asset-2", Duration.ofSeconds(2).toString(), RSI14_THRESH_BELOW30);
			IndicatorSubscription four = new IndicatorSubscription("asset-2", Duration.ofSeconds(2).toString(), "RSI14-ThreshAbove60");
			
			return Arrays.asList(one, two, three);
		}
		
		public static Flux<List<IndicatorSubscription>> confFluxSingleUpdate(){
		
			return Flux.create(confSink->{
		
			Mono.delay(Duration.ofSeconds(1)).subscribe(l-> confSink.next(IndicatorTestDataUtil.getSingleTestConfiguration()));
			Mono.delay(Duration.ofSeconds(3)).subscribe(l-> confSink.complete());
			
			});
		}
		
		public static Flux<TestDataInputType> dataFluxSingleInput(){
			
			return Flux.create(confSink->{
				
				
				Mono.delay(Duration.ofSeconds(2)).subscribe(l-> confSink.next(IndicatorTestDataUtil.getTestData().get(0)));
				Mono.delay(Duration.ofSeconds(4)).subscribe(l-> confSink.complete());
				
				});
			
		}
		
		public static Flux<TestDataInputType> dataFluxDualInputUniqueSKs(){
			
			return Flux.create(confSink->{
				
				TestDataInputType one = new TestDataInputType("asset-1", Duration.ofSeconds(1), LocalDateTime.now(), 5142.990459018316);
				TestDataInputType two = new TestDataInputType("asset-1", Duration.ofHours(1), LocalDateTime.now(), 5142.990459018316); // false second
				
				Mono.delay(Duration.ofSeconds(2)).subscribe(l-> confSink.next(two));
				Mono.delay(Duration.ofSeconds(3)).subscribe(l-> confSink.next(one));
				Mono.delay(Duration.ofSeconds(4)).subscribe(l-> confSink.complete());
				
				});
			
		}

		/**
		 * A flux representing a user specified indicator
		 * with custom properties
		 * 
		 * @return
		 */
		public static Flux<List<IndicatorConfigurationSpec>> indicatorTestConfigurations() {
			
			return Flux.create(sink->{
				
				IndicatorConfigurationSpec spec = new IndicatorConfigurationSpec(RSI14_THRESH_BELOW30, 
					List.of(	
						new IndicatorFnConfigSpec("1-RSI", Map.of("steps", 14.0), IndicatorFNType.Transform, IndicatorFnIdentity.TRANSFORM_RSI),
						new IndicatorFnConfigSpec("2-Drops below threshold", Map.of("value", 30.0), IndicatorFNType.Alert, IndicatorFnIdentity.ALERT_THRESHOLD_BELOW)
					)
				);
				
				sink.next(Arrays.asList(spec));
				
			});
		}
		
		public static MutableIndicatorStateManager stubMutableIndicatorStateManager() {
			
			IndicatorAttributeState h = new IndicatorAttributeState(Map.of(
					"1-RSI-rsi-value", 7935.522040170545,
					"1-RSI-rsi-ave-loss", 132.82257418166498,
					"1-RSI-rsi-rsi",35.19557443409366,
					"1-RSI-rsi-ave-gain",72.13653628307311,
					"2-Drops below threshold-prev-state",35.19557443409366
					));
			IndicatorTestResult state = new IndicatorTestResult(Optional.empty(),
								new IndicatorTestSpec(0, 0.0, LocalDateTime.now().minusYears(2), h));
			
			MutableIndicatorStateManager indicatorStateMan = new MutableIndicatorStateManager(Map.of("asset-1" + "-" + "PT1S" + "-" + "RSI-14-ThreshBelow30", state));
			
			return indicatorStateMan;
			
		}
	
}
