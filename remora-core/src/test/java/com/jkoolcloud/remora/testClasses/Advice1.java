package com.jkoolcloud.remora.testClasses;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class Advice1 implements RemoraAdvice {
	private static final Logger LOGGER = Logger.getLogger(Advice1.class.getName());

	public Advice1() {
		LOGGER.info("Initialsed");
	}

	@Override
	public void install(Instrumentation inst) {

	}
}
