package com.jkoolcloud.remora;

import org.junit.Test;
import org.tinylog.configuration.Configuration;

public class RemoraInitTest {

	@Test
	public void configureLogger() {
		System.setProperty(Remora.REMORA_PATH, ".");
		RemoraInit init = new RemoraInit();
		// Remora.configureRemoraRootLogger(".");
		// init.configureAdviceLogger(new Advice1());
		// init.configureAdviceLogger(new Advice2());

	}

	public static void main(String[] args) {
		String key = "writer" + "NO";
		String adviceName = "NO";

		org.tinylog.Logger.tag("INIT").info("HI");
		Configuration.set(key, "rolling file");
		Configuration.set(key + ".file", adviceName + ".log");
		Configuration.set(key + ".format", " {level}: {message}");
		Configuration.set(key + ".tag", adviceName);
		Configuration.set(key + ".level", "debug");
		org.tinylog.Logger.tag("NO").info("HI");
		org.tinylog.Logger.tag("ED").info("ED");

		org.tinylog.Logger.info("HI");
	}

}
