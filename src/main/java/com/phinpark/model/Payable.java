package com.phinpark.model;

import java.time.Duration;

public interface Payable {
	public double calculatefee(Duration duration);
}
