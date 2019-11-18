package com.jkoolcloud.remora.advices;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class JDBCConnectionAdviceTest {

	@Test
	public void testJDBCConnectionAdviceInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(JDBCConnectionAdvice.class);

		// Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

		// test before method
		// JDBCConnectionAdvice.before();

		// test after method
		// JDBCConnectionAdvice.after();
	}
}