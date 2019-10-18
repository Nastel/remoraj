package com.jkoolcloud.remora.advices;

import java.lang.reflect.Method;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.jkoolcloud.remora.core.EntryDefinition;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class JBossAdviceTest {

	@Test
	public void testJBossInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(JBossAdvice.class);

		Method method = Whitebox.getMethod(Object.class, "toString");

		// test before method
		// JBossAdvice.before();

		// test after method
		// JBossAdvice.after();
	}
}