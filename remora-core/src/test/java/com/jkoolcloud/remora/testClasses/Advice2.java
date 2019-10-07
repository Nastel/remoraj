package com.jkoolcloud.remora.testClasses;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class Advice2 implements RemoraAdvice {
	private static final Logger LOGGER = Logger.getLogger(Advice2.class.getName());

	public Advice2() {
		LOGGER.info("Initialsed1");
	}

	@Override
	public void install(Instrumentation inst) {

	}
}
