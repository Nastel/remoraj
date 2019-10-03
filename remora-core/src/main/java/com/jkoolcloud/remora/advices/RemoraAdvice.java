package com.jkoolcloud.remora.advices;

import java.lang.instrument.Instrumentation;

public interface RemoraAdvice {
	void install(Instrumentation inst);
}
