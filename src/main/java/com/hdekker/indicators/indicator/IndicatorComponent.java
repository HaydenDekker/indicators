package com.hdekker.indicators.indicator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.ConfigManger;
import com.hdekker.indicators.indicator.components.SampleSubscriber;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.fn.Indicator;
import com.hdekker.indicators.indicator.state.State;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorStateManager;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Top level 0 indicator spec.
 * TODO allow exposure of internal state for
 * persistance.
 * 
 * @author HDekker
 *
 */
public class IndicatorComponent {

	/**
	 * A state object that can hold 
	 * the PK, and a Map of SK's to Indicator
	 * 
	 * Set by the config manager
	 * Used by the sampler subscriber
	 * State hidden from user 
	 * 
	 * @return
	 */
	public static IndicatorConfigState getIndicatorConfigManagerInstance() {
		Map<String, Map<String, List<String>>> indicatorState = Map.of();
		IndicatorConfigState state = new IndicatorConfigState(indicatorState);
		return state;
	}
	
	/**
	 * Map<PK-SK-Indicator-Id, Tuple2<IndicatorState, IndicatorPort>
	 * 
	 * A Primary and Secondary indentifier together with and indicator id
	 * produce a tuple2 contaning the state of the indicator and the port
	 * associated with the key.
	 * 
	 * Indicators may have multipl inputs hence the same indicator consuming
	 * multiple assets
	 * 
	 */
	public static IndicatorStateManager getIndicatorStateInstance(){
		return new IndicatorStateManager(Map.of()); 
	}
	
	/***
	 * 	Implemenation of state
	 *  For primary key, return mapping wrapped in optional.
	 * 
	 */
	static Function<State<Map<String, Map<String, Indicator>>>,
		Function<String, Optional< Map<String, Indicator>>>> pkMap = (stateManager) -> (s)-> Optional.ofNullable(stateManager.getState().get(s));
	
		
	static class MutableStateHolder<T>{
		
		T state;
		
		public T getState() {
			return state;
		}
		public void setState(T state) {
			this.state = state;
		}
		
	}
	
	public static class IndicatorComponentInputSpec<T>{
		
		final Flux<List<IndicatorSubscription>> subscriptionConfigFlux;
		final Flux<List<IndicatorConfigurationSpec>> indicatorConfigFlux;
		final Flux<T> dataFlux;
		
		public IndicatorComponentInputSpec(Flux<List<IndicatorSubscription>> subscriptionConfigFlux,
				Flux<List<IndicatorConfigurationSpec>> indicatorConfigFlux, Flux<T> dataFlux) {
			super();
			this.subscriptionConfigFlux = subscriptionConfigFlux;
			this.indicatorConfigFlux = indicatorConfigFlux;
			this.dataFlux = dataFlux;
		}

		public Flux<List<IndicatorSubscription>> getSubscriptionConfigFlux() {
			return subscriptionConfigFlux;
		}

		public Flux<List<IndicatorConfigurationSpec>> getIndicatorConfigFlux() {
			return indicatorConfigFlux;
		}

		public Flux<T> getDataFlux() {
			return dataFlux;
		}
		
	}
	
	/**
	 * Top component
	 * 
	 * @author Hayden Dekker
	 *
	 * @param <K>
	 */
	public interface IndicatorComponentBuilder<K extends IndicatorSampleData> extends 
	Function<IndicatorComponentInputSpec<K>, 
	Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorStateManager>>, Flux<List<Tuple3<IndicatorEvent, K, IndicatorDetails>>>>> {}
	
	
	public static <K extends IndicatorSampleData> 
	IndicatorComponentBuilder<K> instanceWithNewState(){
		
		return instance(Tuples.of(getIndicatorConfigManagerInstance(), getIndicatorStateInstance()));
		
	}
		
	/**
	 * Builder 
	 * @param <T>
	 * @param <K>
	 * @return
	 */
	public static 
		<K extends IndicatorSampleData> 
		IndicatorComponentBuilder<K> instance(
			Tuple2<IndicatorConfigState, IndicatorStateManager> initialState){
		
		MutableStateHolder<IndicatorConfigState> confState = new MutableStateHolder<IndicatorConfigState>();
		confState.setState(initialState.getT1());
		
		MutableStateHolder<IndicatorStateManager> intState = new MutableStateHolder<IndicatorStateManager>();
		intState.setState(initialState.getT2());
		
		return (configSpec)->{
			
			ConfigManger confMan = ConfigManger.buildStandardConfMan();
			Flux<Tuple2<IndicatorConfigState, IndicatorStateManager>> outConf = 
					confMan.withInputs(
							new ConfigManger.ConfigManagerConfigSpec(
									configSpec.getSubscriptionConfigFlux(), 
									configSpec.getIndicatorConfigFlux(),
									confState::getState, 
									confState::setState, 
									intState::getState, 
									intState::setState)
					);
			
			SampleSubscriber<K> samp = SampleSubscriber.builder();
			Flux<List<Tuple3<IndicatorEvent, K, IndicatorDetails>>> outSamp = samp.withInputs(Tuples.of(configSpec.getDataFlux(), 
							(primaryKey) -> Optional.ofNullable(confState.getState().getState().get(primaryKey)),
							(indKey) -> intState.getState().getState().get(indKey)
			));
			
			return Tuples.of(outConf, outSamp);
		}; 
		
	}
	
}
