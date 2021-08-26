package com.hdekker.indicators.indicator;

import java.time.Duration;
import java.time.LocalDateTime;

public class TestDataInputType implements IndicatorSampleData{
	
	final String assetId;
	final Duration sampleRate;
	final LocalDateTime sampleDateTime;
	final Double value;

	public TestDataInputType(String assetId, Duration sampleRate, LocalDateTime sampleDateTime, Double value) {
		super();
		this.assetId = assetId;
		this.sampleRate = sampleRate;
		this.sampleDateTime = sampleDateTime;
		this.value = value;
	}

	public Double getValue() {
		return value;
	}

	public Duration getSampleRate() {
		return sampleRate;
	}

	public String getAssetId() {
		return assetId;
	}
	
	public LocalDateTime getSampleDateTime() {
		return sampleDateTime;
	}

	@Override
	public String getPrimaryKey() {
		// TODO Auto-generated method stub
		return getAssetId();
	}

	@Override
	public String getSecondaryKey() {
		// TODO Auto-generated method stub
		return getSampleRate().toString();
	}
	
	
	
}
