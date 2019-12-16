package com.jkoolcloud.remora.advices;

import org.junit.Test;

import com.jkoolcloud.remora.core.EntryDefinition;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Method;


//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class BankBenchmarkAdviceTest{

	@Test
	public void testBankBenchmarkInterceptor() throws NoSuchMethodException{
		//PowerMockito.mockStatic(<<classToIntercept>>.class);
		//WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry=new EntryDefinition(BankBenchmarkAdvice.class);

//		Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

		//test before method
//		BankBenchmarkAdvice.before();

		//test after method
//		BankBenchmarkAdvice.after();
	}
}