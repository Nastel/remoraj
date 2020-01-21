package com.jkoolcloud.remora.advices;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class SpringServiceAdviceTest {

	@Test
	public void testSpringServiceInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(SpringServiceAdvice.class);

		// Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

		// test before method
		// SpringServiceAdvice.before();

		// test after method
		// SpringServiceAdvice.after();
	}
}