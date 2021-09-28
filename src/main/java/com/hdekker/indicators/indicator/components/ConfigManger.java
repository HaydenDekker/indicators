package com.hdekker.indicators.indicator.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.plaf.ListUI;

import com.hdekker.indicators.indicator.IndicatorConfigurationSpec;
import com.hdekker.indicators.indicator.IndicatorFactory;
import com.hdekker.indicators.indicator.IndicatorSubscription;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorStateManager;
import com.hdekker.indicators.indicator.state.impl.MutableAttributeStateHolder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

/**
 * Converts stream of indicator configuration
 * to initialised indicators list per assigned sample.
 * 
 * Pushes flux out so that the subscription and error
 * can occur at the top level.
 * 
 * Any existing conf will have state reused in 
 * new state map. All new conf will have initialised state.
 * 
 * Doesn't care about indicator
 * 
 * @author HDekker
 *
 * @param <T>
 */
public interface ConfigManger {
	
	public class ConfigManagerConfigSpec {
		
		final Flux<List<IndicatorSubscription>> subscriptionFlux;
		final Flux<List<IndicatorConfigurationSpec>> configurationFlux;
		final Supplier<IndicatorConfigState> configurationStateSupplier;
		final Consumer<IndicatorConfigState> configurationStateUpdater;
		final Supplier<IndicatorStateManager> indicatorStateSupplier;
		final Consumer<IndicatorStateManager> indicatorStateUpdater;
		
		public ConfigManagerConfigSpec(Flux<List<IndicatorSubscription>> subscriptionFlux,
				Flux<List<IndicatorConfigurationSpec>> configurationFlux,
				Supplier<IndicatorConfigState> configurationStateSupplier,
				Consumer<IndicatorConfigState> configurationStateUpdater,
				Supplier<IndicatorStateManager> indicatorStateSupplier,
				Consumer<IndicatorStateManager> indicatorStateUpdater) {
			super();
			this.subscriptionFlux = subscriptionFlux;
			this.configurationFlux = configurationFlux;
			this.configurationStateSupplier = configurationStateSupplier;
			this.configurationStateUpdater = configurationStateUpdater;
			this.indicatorStateSupplier = indicatorStateSupplier;
			this.indicatorStateUpdater = indicatorStateUpdater;
		}
		public Flux<List<IndicatorSubscription>> getSubscriptionFlux() {
			return subscriptionFlux;
		}
		public Flux<List<IndicatorConfigurationSpec>> getConfigurationFlux() {
			return configurationFlux;
		}
		public Supplier<IndicatorConfigState> getConfigurationStateSupplier() {
			return configurationStateSupplier;
		}
		public Consumer<IndicatorConfigState> getConfigurationStateUpdater() {
			return configurationStateUpdater;
		}
		public Supplier<IndicatorStateManager> getIndicatorStateSupplier() {
			return indicatorStateSupplier;
		}
		public Consumer<IndicatorStateManager> getIndicatorStateUpdater() {
			return indicatorStateUpdater;
		}
		
	}
	
	Flux<Tuple2<IndicatorConfigState,
	IndicatorStateManager>> withInputs(ConfigManagerConfigSpec input);
	
	public static ConfigManger buildStandardConfMan(){
		
			return (config) -> {
				
				// listen for indicator definition updates
				config.getConfigurationFlux()
					.subscribe(indConfigUpdate->
						indConfigUpdate.forEach(indConfig->
							IndicatorFactory.configureIndicator(indConfig)
						)
				);
				
				// listen for subscriptions
				return config.getSubscriptionFlux().map(l->{
					
					Map<String, Map<String, List<String>>> items = convertForFiltering.apply(l);
					List<Tuple3<String, String, String>> newItems = flattenState.apply(items);
					List<Tuple3<String, String, String>> existingItems = findExistingConfig.apply(items, config.getConfigurationStateSupplier().get().getState());
					
					Map<String, Tuple2<MutableAttributeStateHolder, Integer>> 
						newMap = IndicatorStateManager.getInternalStateMap
							.apply(IndicatorStateManager.withNewInternalState)
							.apply(newItems);
					
					Map<String, Tuple2<MutableAttributeStateHolder, Integer>> existingMap = IndicatorStateManager.getInternalStateMap
							.apply(IndicatorStateManager.withExistingInternalState.apply(config.getIndicatorStateSupplier().get().getState()))
							.apply(existingItems);
					
					Map<String, Tuple2<MutableAttributeStateHolder, Integer>> combined = new HashMap<>();
					combined.putAll(newMap);
					combined.putAll(existingMap);
					
					IndicatorStateManager iism = new IndicatorStateManager(combined);
					IndicatorConfigState ics = new IndicatorConfigState(items);
					
					// update global state
					config.getIndicatorStateUpdater().accept(iism);
					config.getConfigurationStateUpdater().accept(ics);
					
					return Tuples.of(ics, iism);
				});
				
			};

		
	}
	
	// map functions for input conversion fn1
	Function<IndicatorSubscription, String> keyMap = (is) -> is.getAssetPrimaryKey();
	
	Function<IndicatorSubscription, Map<String, List<String>>> valMap = (in) -> Map.of(in.getAssetSortKey(), List.of(in.getIndicatorToSubscribe()));
	
	/***
	 * Will only every have a single
	 * indicator per subscription
	 * 
	 * This checks if multiple indicators apply
	 * to the same asset and sortkey then
	 * mergers those indicators to a single
	 * list.
	 * 
	 * TODO as users define indicators, many
	 * indicators may be identical but have 
	 * different name. Waste of computation
	 * 
	 */
	BinaryOperator<Map<String, List<String>>> merge = (prev, nxt) -> {
		
		HashMap<String, List<String>> map = new HashMap<>(prev);
		nxt.entrySet()
			.forEach(entry->{
			map.merge(entry.getKey(), 
					entry.getValue(), 
					(p,n) -> {
						List<String> l = new ArrayList<>();
						l.addAll(n);
						l.addAll(p);
						return l;
					});
		});
		return map;
		
	};
	
	Function<List<IndicatorSubscription>, Map<String, Map<String, List<String>>>>
				convertForFiltering = (list)->{
					
					Map<String, Map<String, List<String>>> conv = list.stream()
							.collect(Collectors.toMap(keyMap, 
											valMap, merge));

					return conv;
	};
	
	/**
	 * Take individual config items
	 * move them into state ready for
	 * quick access.
	 * 
	 */
	Function<List<Tuple3<String, String, String>>,
			Map<String, Map<String, List<String>>>> buildState
		= (items) -> {
			
			Function<Tuple3<String, String, String>, String> keyMap = (in) -> in.getT1();
			Function<Tuple3<String, String, String>, Map<String, List<String>>> valueMap = (in) -> {
				return Map.of(in.getT2(), List.of(in.getT3()));
			};
			BinaryOperator<Map<String, List<String>>> merge = (pre, nxt) -> {
				HashMap<String, List<String>> map = new HashMap<>();
				map.putAll(pre);
				nxt.entrySet().forEach(entry->{
					map.merge(entry.getKey(), entry.getValue(), (pre1, nxt1) -> {
						
						List<String> l = new ArrayList<>(pre1);
						l.addAll(nxt1);
						
						return l;
						
					});
				});
				return map;
			};
			
			return items.stream()
							.collect(Collectors.toMap(keyMap, valueMap, merge));
		};
	
	
	
	/**
	 * Break down existing state to allow for
	 * easy comparison.
	 * 
	 */
	Function<Map<String, Map<String, List<String>>>,
						List<Tuple3<String, String, String>>> flattenState
		
		= (map)->{
			
			return map.entrySet()
					.stream()
					.flatMap(entry-> {
						
						return entry.getValue().entrySet()
								.stream()
								.flatMap(innerEntry-> {
									
									return innerEntry.getValue()
											.stream()
											.map(value-> Tuples.of(entry.getKey(), innerEntry.getKey(), value));
					
								});
					}).collect(Collectors.toList());
			
		};
	
	/**
	 * Check if entry existing in a config
	 * state
	 * 
	 */
	BiPredicate<Map<String, Map<String, List<String>>>,
		Tuple3<String, String, String>> entryExists =
		(map, entry)->{
			
			return Optional.ofNullable(map.get(entry.getT1()))
							.map(m-> m.get(entry.getT2()))
							.map(l-> l.contains(entry.getT3()))
							.isPresent();
	};
	
	/**
	 * Return all items in the previous list
	 * that are still in the new list.
	 * 
	 */
	BiFunction<
				Map<String, Map<String, List<String>>>, 
				Map<String, Map<String, List<String>>>, 
				List<Tuple3<String, String, String>>> findExistingConfig
		
		= (current, previous) -> {
			
			return flattenState.apply(current)
				.stream()
				.filter(entry-> entryExists.test(previous, entry))
				.collect(Collectors.toList());
			
		};
	
}
