package com.hdekker.indicators.indicator;

/**
 * An interface to allow data to be consumed
 * by indicator, holds the source for convenience 
 * passing through to the output.
 * 
 * 
 * @author HDekker
 *
 * @param <T>
 */
public interface IndicatorSampleData {

	Double getValue();
	String getPrimaryKey();
	String getSecondaryKey();
	
}
