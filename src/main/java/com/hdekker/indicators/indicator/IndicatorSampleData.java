package com.hdekker.indicators.indicator;

import java.time.LocalDateTime;

/**
 * An interface to allow data to be consumed
 * by indicator
 * 
 * 
 * @author HDekker
 *
 */
public interface IndicatorSampleData {

	Double getValue();
	String getPrimaryKey();
	String getSecondaryKey();
	LocalDateTime getSampleDateTime();
	
}
