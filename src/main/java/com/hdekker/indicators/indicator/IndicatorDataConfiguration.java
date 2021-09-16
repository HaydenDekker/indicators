package com.hdekker.indicators.indicator;

import java.util.List;

/**
 * 
 * Simple interface to associate a 
 * type of input data with an indicator.
 * 
 * 
 * @author HDekker
 *
 */
public interface IndicatorDataConfiguration{

	String getAssetPrimaryKey();
	String getAssetSecondaryKey();
	List<String> getIndicatorId();

}
