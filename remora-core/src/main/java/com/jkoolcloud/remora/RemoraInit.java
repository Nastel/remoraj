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

		/*
		 * new IBMWebsphereInterceptor().getTransform().installOn(inst);
		 * 
		 * new IBMAdapterRSA().getTransform().installOn(inst); new
		 * DataSourceConnectionAdvice().getTransform().with(AgentBuilder.Listener.StreamWriting.toSystemError()).
		 * installOn(inst); // new JavaxHttpServlet().getTransform().installOn(inst); new
		 * JMSSendAdvice().getTransform().installOn(inst); new JMSReceiveAdvice().getTransform().installOn(inst); new
		 * JMSCreateConnectionAdvice().getTransform().installOn(inst); //new
		 * SimpleTest2().getTransform().installOn(inst); new SimpleTest().getTransform().installOn(inst);
		 */
	}
}
