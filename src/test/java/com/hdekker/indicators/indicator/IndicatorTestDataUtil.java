package com.hdekker.indicators.indicator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
		
		public static List<TestConfiguration> getSingleTestConfiguration(){
			
			TestConfiguration one = new TestConfiguration("asset-1", Duration.ofSeconds(1), List.of(RSI14_THRESH_BELOW30));
			return Arrays.asList(one);
		}
		
		public static List<TestConfiguration> getMultiTestConfiguration(){
			
			TestConfiguration one = new TestConfiguration("asset-1", Duration.ofSeconds(1), List.of(RSI14_THRESH_BELOW30));
			TestConfiguration two = new TestConfiguration("asset-2", Duration.ofSeconds(1), List.of(RSI14_THRESH_BELOW30));
			
			return Arrays.asList(one, two);
		}
		
		public static List<TestConfiguration> getMultiTestConfWithMultiIndicators(){
			
			TestConfiguration one = new TestConfiguration("asset-1", Duration.ofSeconds(1), List.of(RSI14_THRESH_BELOW30));
			TestConfiguration two = new TestConfiguration("asset-2", Duration.ofSeconds(1), List.of(RSI14_THRESH_BELOW30));
			TestConfiguration three = new TestConfiguration("asset-2", Duration.ofSeconds(2), List.of(RSI14_THRESH_BELOW30, "RSI14-ThreshAbove60"));
			
			return Arrays.asList(one, two, three);
		}
		
		public static Flux<List<TestConfiguration>> confFluxSingleUpdate(){
		
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
	
}
