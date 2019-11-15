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
public class WebsocketSendAdviceTest{

	@Test
	public void testWebsocketSendInterceptor() throws NoSuchMethodException{
		//PowerMockito.mockStatic(<<classToIntercept>>.class);
		//WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry=new EntryDefinition(WebsocketSendAdvice.class);

//		Method method=Whitebox.getMethod(Object.class,"<<interceptingMethod>>");

		//test before method
//		WebsocketSendAdvice.before();

		//test after method
//		WebsocketSendAdvice.after();
	}
}