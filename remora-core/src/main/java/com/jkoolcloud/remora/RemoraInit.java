package com.jkoolcloud.remora;

import static com.jkoolcloud.remora.core.utils.LoggerWrapper.pLog;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.jkoolcloud.remora.advices.RemoraAdvice;

public class RemoraInit {

	public void initializeAdvices(Instrumentation inst, ClassLoader classLoader) {
		pLog("Initializing advices: " + this.getClass() + " classloader: " + this.getClass().getClassLoader());

		ServiceLoader<RemoraAdvice> advices = ServiceLoader.load(RemoraAdvice.class, classLoader);

		Iterator<RemoraAdvice> iterator = advices.iterator();
		while (iterator.hasNext()) {
			RemoraAdvice remoraAdvice = iterator.next();
			RemoraConfig.configure(remoraAdvice);
			remoraAdvice.install(inst);

			pLog("Found module: " + remoraAdvice);

		}
		pLog("Loading finished");

	}
}
