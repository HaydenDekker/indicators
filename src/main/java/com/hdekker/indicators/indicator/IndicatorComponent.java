package com.hdekker.indicators.indicator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.components.ConfigManger;
import com.hdekker.indicators.indicator.components.SampleSubscriber;
import com.hdekker.indicators.indicator.components.SampleSubscriber.IndicatorDetails;
import com.hdekker.indicators.indicator.state.IndicatorStateManager;
import com.hdekker.indicators.indicator.state.impl.IndicatorConfigState;
import com.hdekker.indicators.indicator.state.impl.IndicatorInternalStateManager;

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
public interface IndicatorComponent {

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
	public static IndicatorInternalStateManager getIndicatorStateInstance(){
		return new IndicatorInternalStateManager(Map.of()); 
	}
	
	/***
	 * 	Implemenation of state
	 *  For primary key, return mapping wrapped in optional.
	 * 
	 */
	static Function<IndicatorStateManager<Map<String, Map<String, Indicator>>>,
		Function<String, Optional< Map<String, Indicator>>>> pkMap = (stateManager) -> (s)-> Optional.ofNullable(stateManager.getState().get(s));
	
		
	class MutableStateHolder<T>{
		
		T state;
		
		public T getState() {
			return state;
		}
		public void setState(T state) {
			this.state = state;
		}
		
	}
	
	public interface IndicatorComponentBuilder<T extends IndicatorConfigurationData, K extends IndicatorSampleData> extends 
	Function<Tuple2<Flux<List<T>>, Flux<K>>, 
	Tuple2<Flux<Tuple2<IndicatorConfigState, IndicatorInternalStateManager>>, Flux<List<Tuple3<IndicatorEvent, K, IndicatorDetails>>>>> {}
	
	
	public static <T extends IndicatorConfigurationData, K extends IndicatorSampleData> 
	IndicatorComponentBuilder<T,K> instanceWithNewState(){
		
		return instance(Tuples.of(getIndicatorConfigManagerInstance(), getIndicatorStateInstance()));
		
	}
		
	/**
	 * Builder 
	 * @param <T>
	 * @param <K>
	 * @return
	 */
	public static <T extends IndicatorConfigurationData, K extends IndicatorSampleData> 
		IndicatorComponentBuilder<T,K> instance(
								Tuple2<IndicatorConfigState, IndicatorInternalStateManager> initialState){
		
		MutableStateHolder<IndicatorConfigState> confState = new MutableStateHolder<IndicatorConfigState>();
		confState.setState(initialState.getT1());
		MutableStateHolder<IndicatorInternalStateManager> intState = new MutableStateHolder<IndicatorInternalStateManager>();
		intState.setState(initialState.getT2());
		
		return (tuplConNSamples)->{
			
			ConfigManger<T> confMan = ConfigManger.buildStandardConfMan();
			Flux<Tuple2<IndicatorConfigState, IndicatorInternalStateManager>> outConf = confMan.withInputs(Tuples.of(tuplConNSamples.getT1(), confState::getState, confState::setState, intState::getState, intState::setState));
			
			SampleSubscriber<K> samp = SampleSubscriber.builder();
			Flux<List<Tuple3<IndicatorEvent, K, IndicatorDetails>>> outSamp = samp.withInputs(Tuples.of(tuplConNSamples.getT2(), 
							(primaryKey) -> Optional.ofNullable(confState.getState().getState().get(primaryKey)),
							(indKey) -> intState.getState().getState().get(indKey)
			));
			
			return Tuples.of(outConf, outSamp);
		}; 
		
	}
	
}
