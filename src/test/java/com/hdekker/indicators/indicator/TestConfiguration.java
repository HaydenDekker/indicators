package com.hdekker.indicators.indicator;

import java.time.Duration;
import java.util.List;

public class TestConfiguration extends IndicatorSubscription {

	public TestConfiguration(String primaryKey, Duration secondaryKey, String indicatorId) {
		super(primaryKey, secondaryKey.toString(), indicatorId);
	}

}
