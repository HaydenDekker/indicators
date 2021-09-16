package com.hdekker.indicators.indicator.transform;


public class WilderRSIInstant {

	//LocalDateTime time;
	Double value = 0.0;
	Double avGain = 0.0;
	Double avLoss = 0.0;
	Double rsi = 0.0;
	
//	public LocalDateTime getTime() {
//		return time;
//	}
//	public void setTime(LocalDateTime time) {
//		this.time = time;
//	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public Double getAvGain() {
		return avGain;
	}
	public void setAvGain(Double avGain) {
		this.avGain = avGain;
	}
	public Double getAvLoss() {
		return avLoss;
	}
	public void setAvLoss(Double avLoss) {
		this.avLoss = avLoss;
	}
	public Double getRsi() {
		return rsi;
	}
	public void setRsi(Double rsi) {
		this.rsi = rsi;
	}

}
