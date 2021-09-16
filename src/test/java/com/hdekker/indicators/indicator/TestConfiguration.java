package com.hdekker.indicators.indicator;

import java.time.Duration;
import java.util.List;

public class TestConfiguration implements IndicatorData {

	final String primaryKey;
	final String secondaryKey;
	final List<String> indicatorId;
	
	public TestConfiguration(String primaryKey, Duration secondaryKey, List<String> indicatorId) {
		super();
		this.primaryKey = primaryKey;
		this.secondaryKey = secondaryKey.toString();
		this.indicatorId = indicatorId;
	}

	@Override
	public String getAssetPrimaryKey() {
		return this.primaryKey;
	}

	@Override
	public String getAssetSecondaryKey() {
		
		return this.secondaryKey;
	}

	@Override
	public List<String> getIndicatorId() {
		
		return this.indicatorId;
	}

	

}
