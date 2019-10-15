package com.jkoolcloud.remora;

import java.util.logging.Logger;

import org.junit.Test;

public class RemoraInitTest {

	@Test
	public void configureLogger() {
		System.setProperty(Remora.REMORA_PATH, ".");
		Logger logger = Logger.getLogger(getClass().getName());
		RemoraInit init = new RemoraInit();
		Remora.configureRemoraRootLogger(".");
		// init.configureAdviceLogger(new Advice1());
		// init.configureAdviceLogger(new Advice2());

	}

}