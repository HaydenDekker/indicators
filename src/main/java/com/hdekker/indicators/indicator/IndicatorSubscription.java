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
public class IndicatorSubscription{

	final String assetPrimaryKey;
	final String assetSortKey;
	final String indicatorToSubscribe;
	
	public IndicatorSubscription(String assetPrimaryKey, String assetSortKey, String indicatorToSubscribe) {
		this.assetPrimaryKey = assetPrimaryKey;
		this.assetSortKey = assetSortKey;
		this.indicatorToSubscribe = indicatorToSubscribe;
	}
	public String getAssetPrimaryKey() {
		return assetPrimaryKey;
	}
	public String getAssetSortKey() {
		return assetSortKey;
	}
	public String getIndicatorToSubscribe() {
		return indicatorToSubscribe;
	}
	
	

}
