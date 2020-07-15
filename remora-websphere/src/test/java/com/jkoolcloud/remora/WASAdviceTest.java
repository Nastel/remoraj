/*
 * Copyright 2019-2020 NASTEL TECHNOLOGIES, INC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jkoolcloud.remora;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Collections;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.ibm.ws.webcontainer.servlet.ServletWrapper;
import com.ibm.ws.webcontainer.webapp.WebApp;
import com.jkoolcloud.remora.advices.BaseTransformers;
import com.jkoolcloud.remora.advices.WASAdvice;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.SysOutOutput;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WebApp.class })
@SuppressStaticInitializationFor({ "com.ibm.ws.webcontainer.webapp.WebApp",
		"com.ibm.ws.webcontainer.servlet.ServletWrapper" })
public class WASAdviceTest {

	static {
		System.setProperty(Remora.REMORA_PATH, ".");
		AdviceRegistry.INSTANCE.report(Collections.singletonList(new WASAdvice()));
	}

	private BaseTransformers.InterceptionContext ctx = new BaseTransformers.InterceptionContext();

	@Test
	public void testWebsphereInterceptor() throws NoSuchMethodException {

		// System.setProperty("probe.output", SoutOutput.class.getName());

		PowerMockito.mockStatic(WebApp.class);
		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(WASAdvice.class, true);

		ServletResponse servletResponse = mock(ServletResponse.class);
		// Method method = WebApp.class.getMethod("handleRequest", ServletRequest.class, ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);

		WASAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, ctx, 0);
		WASAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, ctx, 0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testWebsphereInterceptorWithJMSCall() throws NoSuchMethodException {

		System.setProperty("probe.output", SysOutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);
		MessageProducer messageProducer = mock(MessageProducer.class);

		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
		Method jmxMethod = Whitebox.getMethod(MessageProducer.class, "send", Message.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(WASAdvice.class, true);
		// EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);

		WASAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, ctx, 0);
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 56);
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 78 );
		WASAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, ctx, 140);
	}

	@SuppressWarnings("unused")
	@Test
	public void testWebsphereInterceptorWithDuplicateJMSCall() throws NoSuchMethodException {

		// System.setProperty("probe.output", SoutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = mock(ServletRequest.class);
		MessageProducer messageProducer = mock(MessageProducer.class);
		MessageConsumer messageConsumer = mock(MessageConsumer.class);

		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
		Method jmxMethod = Whitebox.getMethod(MessageProducer.class, "send", Message.class);
		Method jmxReceiveMethod = Whitebox.getMethod(MessageConsumer.class, "receive");

		EntryDefinition handleRequestEntry = new EntryDefinition(WASAdvice.class, true);
		// EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);
		// EntryDefinition jmsSendEntryInternal = new EntryDefinition(JMSSendAdvice.class);
		// EntryDefinition jmsReceiveEntryInternal = new EntryDefinition(JMSReceiveAdvice.class);

		WASAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, ctx, 0);
		// first JMS message
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, System.nanoTime());
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, System.nanoTime());
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, System.nanoTime() );
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, System.nanoTime() );

		// receive
		// WASAdvice.before(messageConsumer, null, jmxReceiveMethod, jmsReceiveEntryInternal, System.nanoTime()
		// );
		// WASAdvice.after(messageConsumer, jmxReceiveMethod, null, null, null,jmsReceiveEntryInternal,
		// System.nanoTime() );
		// second JMS message
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, System.nanoTime());
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, System.nanoTime());
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, System.nanoTime() );
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, System.nanoTime() );

		WASAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, ctx,
				System.nanoTime());
	}

	@SuppressWarnings("unused")
	@Test
	public void testWebsphereInterceptorWithDuplicateJMSCallAndDifferentRequestHandlers() throws NoSuchMethodException {

		System.setProperty("probe.output", SysOutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletWrapper servletWrapper = mock(ServletWrapper.class);
		ServletRequest servletRequest = mock(ServletRequest.class);
		MessageProducer messageProducer = mock(MessageProducer.class);

		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = Whitebox.getMethod(WebApp.class, "handleRequest", ServletRequest.class, ServletResponse.class);
		Method method2 = Whitebox.getMethod(ServletWrapper.class, "handleRequest", ServletRequest.class,
				ServletResponse.class);
		Method jmxMethod = Whitebox.getMethod(MessageProducer.class, "send", Message.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(WASAdvice.class, true);
		// EntryDefinition jmsSendEntry = new EntryDefinition(JMSSendAdvice.class);
		// EntryDefinition jmsSendEntryInternal = new EntryDefinition(JMSSendAdvice.class);

		WASAdvice.before(webApp, servletRequest, servletResponse, method, handleRequestEntry, ctx, 0);
		WASAdvice.after(webApp, method, servletRequest, servletResponse, null, handleRequestEntry, ctx, 140);

		// IBMWebsphereInterceptor.before(servletWrapper, servletRequest, servletResponse, method, handleRequestEntry,
		// 0);
		Object[] jmxSendArguments = {};
		// //first JMS message
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 56);
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, 59);
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, 76 );
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 78 );
		// //second JMS message
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntry, 79);
		// JMSSendAdvice.before(messageProducer, jmxSendArguments, jmxMethod, jmsSendEntryInternal, 86);
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, null, 98 );
		// JMSSendAdvice.after(messageProducer,jmxMethod, jmxSendArguments, null, jmsSendEntry, 102 );
		WASAdvice.after(servletWrapper, method, servletRequest, servletResponse, null, handleRequestEntry, ctx, 140);

	}

	@Test
	public void testWebsphereInterceptorReqNull() throws NoSuchMethodException {

		// System.setProperty("probe.output", SoutOutput.class.getName());

		WebApp webApp = mock(WebApp.class);
		ServletRequest servletRequest = null;

		ServletResponse servletResponse = mock(ServletResponse.class);
		Method method = WebApp.class.getMethod("handleRequest", ServletRequest.class, ServletResponse.class);

		WASAdvice.before(webApp, servletRequest, servletResponse, method, null, ctx, 0);
		WASAdvice.after(webApp, method, servletRequest, servletResponse, null, null, ctx, 0);
	}
}
