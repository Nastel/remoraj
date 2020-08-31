package com.jkoolcloud.remora.advices;

import org.junit.Test;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class ThreadAdviceTest {

	@Test
	public void testThreadInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		// EntryDefinition handleRequestEntry=new EntryDefinition(ThreadAdvice.class);

		// Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

		// test before method
		// ThreadAdvice.before();

		// test after method
		// ThreadAdvice.after();
	}
}
