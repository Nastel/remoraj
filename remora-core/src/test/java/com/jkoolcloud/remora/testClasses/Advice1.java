package com.jkoolcloud.remora.testClasses;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class Advice1 implements RemoraAdvice {
	private static final Logger LOGGER = LoggerFactory.getLogger(Advice1.class.getName());
	private static final String ADVICE_NAME = "1";

	public Advice1() {
		LOGGER.info("Initialsed");
	}

	@Override
	public void install(Instrumentation inst) {

	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}
}
