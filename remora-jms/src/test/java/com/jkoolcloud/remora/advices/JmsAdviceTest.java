package com.jkoolcloud.remora.advices;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class JmsAdviceTest {

	@Test
	public void testWebsphereInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(JMSSendAdvice.class);

		// Method method=Whitebox.getMethod(WebApp.class,"<<interceptingMethod>>");

		// test before method
		// JmsAdvice.before();

		// test after method
		// JmsAdvice.after();
	}
}