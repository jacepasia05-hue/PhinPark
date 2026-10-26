package com.phinpark.model;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;

public class Car extends Vehicle {

	@Override
	public double calculatefee(Duration duration) {
		long minutes = duration.toMinutes();
		return Math.ceil((double) minutes / 60) * 50.0;
	}

	public String getFormattedFee(Duration duration) {
		double fee = calculatefee(duration);
		NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));
		return pesoFormat.format(fee);
	}
}
