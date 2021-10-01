package com.hdekker.indicators.indicator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hdekker.indicators.indicator.IndicatorFnDescriptor;
import com.hdekker.indicators.indicator.IndicatorFnDescriptor.IndicatorFNType;
import com.hdekker.indicators.indicator.alert.IndicatorEvent;
import com.hdekker.indicators.indicator.alert.Threshold;
import com.hdekker.indicators.indicator.fn.ConfigurableIndicatorFn;
import com.hdekker.indicators.indicator.fn.Indicator;
import com.hdekker.indicators.indicator.fn.IndicatorAlert;
import com.hdekker.indicators.indicator.fn.IndicatorTransform;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestResult;
import com.hdekker.indicators.indicator.fn.Indicator.IndicatorTestSpec;
import com.hdekker.indicators.indicator.state.State;
import com.hdekker.indicators.indicator.state.impl.IndicatorAttributeState;
import com.hdekker.indicators.indicator.transform.RSI;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


/**
 * 21-09-2021 - Scrapping idea of indicator static build
 * introducing dynamic fn config
 * 
 * @author Hayden Dekker
 *
 */
public class IndicatorFactory {
	
	/**
	 * Map<String, IndicatorAlert> where String is the unique alert id
	 * 
	 * @author Hayden Dekker
	 *
	 */
	public static class ConfiguredIndicators extends State<Map<String, Indicator>> {

		public ConfiguredIndicators(Map<String, Indicator> state) {
			super(state);
			
		}

	}
	
	public static ConfiguredIndicators configuredIndicators = new ConfiguredIndicators(Map.of());
	
	public static Indicator getIndicator(String name) {
		return configuredIndicators.getState().get(name);
	}

	/**
	 * Staticly defined
	 */
	private static List<IndicatorFnDescriptor> indicatorFnDescriptors = 
			List.of(
				new IndicatorFnDescriptor(IndicatorFnIdentity.ALERT_THRESHOLD_BELOW, Map.of("value", 30.0), IndicatorFNType.Alert),
				new IndicatorFnDescriptor(IndicatorFnIdentity.ALERT_THRESHOLD_ABOVE, Map.of("value", 70.0), IndicatorFNType.Alert),
				new IndicatorFnDescriptor(IndicatorFnIdentity.TRANSFORM_RSI, Map.of("steps", 14.0), IndicatorFNType.Transform)
			);
	
	public static List<IndicatorFnDescriptor> getDescriptors(){
		return indicatorFnDescriptors;
	}
	
	
	private static Map<IndicatorFnIdentity, ConfigurableIndicatorFn<IndicatorAlert>> 
		staticAlertBuilders = Map.of(
			IndicatorFnIdentity.ALERT_THRESHOLD_BELOW, Threshold.movesBelowThresholdAlert(),
			IndicatorFnIdentity.ALERT_THRESHOLD_ABOVE, Threshold.movesAboveThresholdAlert()
				
		); 
	private static Map<IndicatorFnIdentity, ConfigurableIndicatorFn<IndicatorTransform>> 
		staticTransformsBuilders = Map.of(
			IndicatorFnIdentity.TRANSFORM_RSI, RSI.getTransform()
	    );
	
	public static String configureIndicator(IndicatorConfigurationSpec spec) {
		
		// TODO as this grows fn names may not be unique
		// add a check for future
		List<IndicatorTransform> transforms = spec.getConfiguration()
			.stream()
			.filter(conf->conf.getFnType().equals(IndicatorFNType.Transform))
			.map(transConf->staticTransformsBuilders.get(transConf.getFnIdentity())
								.withConfig(transConf))
			.collect(Collectors.toList());
		
		List<IndicatorAlert> alert = spec.getConfiguration()
			.stream()
			.filter(conf->conf.getFnType().equals(IndicatorFNType.Alert))
			.map(alrtConf->staticAlertBuilders.get(alrtConf.getFnIdentity())
					.withConfig(alrtConf))
			.collect(Collectors.toList());
		
		// create indicator
		
		IndicatorTransform transform = transforms.stream()
			.reduce((p,n)-> bind(p, n))
			.get();
		
		// only allowing one alert per indicator currently.
		IndicatorAlert ia = alert.get(0);
		
		Indicator ind = (in) -> {
			
			Tuple2<Optional<IndicatorEvent>, IndicatorTestSpec> res 
				= ia.apply(transform.apply(in));
			
			return new IndicatorTestResult(res.getT1(), res.getT2());
			
		};

		// Set Indicator Factory State TODO don't set state here
		Map<String, Indicator>stateMap = new HashMap<>(configuredIndicators.getState());
		stateMap.put(spec.getIndicatorName(), ind);
		configuredIndicators = new ConfiguredIndicators(stateMap);
		
		return spec.getIndicatorName();
	}
	
	/**
	 * Stupid as the extend clas can't be usef for the
	 * function andThen()
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static IndicatorTransform bind(IndicatorTransform a, IndicatorTransform b) {
		
		return (input)-> {
			return b.apply(a.apply(input));
		};
	}
	
}
