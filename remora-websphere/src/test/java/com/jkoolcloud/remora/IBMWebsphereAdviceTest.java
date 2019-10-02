package com.jkoolcloud.remora;

import com.ibm.ws.webcontainer.servlet.ServletWrapper;
import com.ibm.ws.webcontainer.webapp.WebApp;
import com.jkoolcloud.remora.advices.IBMWebsphereAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.SoutOutput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WebApp.class})
@SuppressStaticInitializationFor({"com.ibm.ws.webcontainer.webapp.WebApp", "com.ibm.ws.webcontainer.servlet.ServletWrapper"})
public class IBMWebsphereAdviceTest {

	@Test
	public void testWebsphereInterceptor() throws NoSuchMethodException {

		//System.setProperty("probe.output", SoutOutput.class.getName());

		PowerMockito.mockStatic(WebApp.class);
		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(IBMWebsphereAdvice.class);


		ServletResponse servletResponse = mock(ServletResponse.class);
		//Method method = WebApp.class.getMethod("handleRequest", ServletRequest.class, ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);

		IBMWebsphereAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, 0);
		IBMWebsphereAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, 0);
	}

	@Test
	public void testWebsphereInterceptorWithJMSCall() throws NoSuchMethodException {

		System.setProperty("probe.output", SoutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);
		MessageProducer messageProducer= mock(MessageProducer.class);


		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
		Method jmxMethod = Whitebox.getMethod(MessageProducer.class,"send", Message.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(IBMWebsphereAdvice.class);
		//EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);


		IBMWebsphereAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, 0);
		Object[] jmxSendArguments = {};
		//JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 56);
		//JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 78 );
		IBMWebsphereAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, 140);
	}

	@Test
	public void testWebsphereInterceptorWithDuplicateJMSCall() throws NoSuchMethodException {

		//System.setProperty("probe.output", SoutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);
		MessageProducer messageProducer= mock(MessageProducer.class);
		MessageConsumer messageConsumer= mock(MessageConsumer.class);


		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
		Method jmxMethod = Whitebox.getMethod(MessageProducer.class,"send", Message.class);
		Method jmxReceiveMethod = Whitebox.getMethod(MessageConsumer.class,"receive");

		EntryDefinition handleRequestEntry = new EntryDefinition(IBMWebsphereAdvice.class);
	//	EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);
//		EntryDefinition jmsSendEntryInternal = new EntryDefinition(JMSSendAdvice.class);
	//	EntryDefinition jmsReceiveEntryInternal = new EntryDefinition(JMSReceiveAdvice.class);


		IBMWebsphereAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, 0);
		Object[] jmxSendArguments = {};
		//first JMS message
//		JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, System.nanoTime());
//		JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal,  System.nanoTime());
	//	JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null,  System.nanoTime() );
//		JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry,  System.nanoTime() );

		//receive
	//	IBMWebsphereAdvice.before(messageConsumer, null, jmxReceiveMethod,  jmsReceiveEntryInternal,  System.nanoTime() );
	//	IBMWebsphereAdvice.after(messageConsumer, jmxReceiveMethod, null, null,  null,jmsReceiveEntryInternal,  System.nanoTime() );
		//second JMS message
//		JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry,  System.nanoTime());
//		JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal,  System.nanoTime());
//		JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null,  System.nanoTime() );
//		JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry,  System.nanoTime() );

		IBMWebsphereAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, System.nanoTime());
	}

    @Test
    public void testWebsphereInterceptorWithDuplicateJMSCallAndDifferentRequestHandlers() throws NoSuchMethodException {

        System.setProperty("probe.output", SoutOutput.class.getName());

        WebApp webApp = mock(WebApp.class);
        ServletWrapper servletWrapper = mock(ServletWrapper.class);
        ServletRequest servletRequest = mock(ServletRequest.class);
        MessageProducer messageProducer= mock(MessageProducer.class);


        ServletResponse servletResponse = mock(ServletResponse.class);
        Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
        Method method2 = Whitebox.getMethod(ServletWrapper.class, "handleRequest", ServletRequest.class, ServletResponse.class);
        Method jmxMethod = Whitebox.getMethod(MessageProducer.class,"send", Message.class);

        EntryDefinition handleRequestEntry = new EntryDefinition(IBMWebsphereAdvice.class);
    //    EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);
     //   EntryDefinition jmsSendEntryInternal = new EntryDefinition(JMSSendAdvice.class);


		IBMWebsphereAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, 0);
		IBMWebsphereAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, 140);

//        IBMWebsphereInterceptor.before(servletWrapper, servletRequest, servletResponse, method, handleRequestEntry, 0);
        Object[] jmxSendArguments = {};
//        //first JMS message
//        JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 56);
//        JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, 59);
//        JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, 76 );
//        JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 78 );
//        //second JMS message
//        JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 79);
//        JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, 86);
//        JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, 98 );
//        JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 102 );
		IBMWebsphereAdvice.after(servletWrapper, method, servletRequest, servletResponse, null, handleRequestEntry, 140);

    }

	@Test
	public void testWebsphereInterceptorReqNull() throws NoSuchMethodException {

		//System.setProperty("probe.output", SoutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = null;

		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = WebApp.class.getMethod("handleRequest", ServletRequest.class, ServletResponse.class);

		IBMWebsphereAdvice.before(webApp, servletRequest, servletResponse, method, null, 0);
		IBMWebsphereAdvice.after(webApp, method, servletRequest, servletResponse, null, null, 0);

	}

}