package com.jkoolcloud.remora.advices;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.SysOutOutput;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class ApacheHttpClientAdviceTest {

	@Test
	public void testApacheHttpClientAdviceInterceptor() throws NoSuchMethodException {
		// PowerMockito.mockStatic(<<classToIntercept>>.class);
		// WebApp webApp=mock(<<classToIntercept>>.class);

		EntryDefinition handleRequestEntry = new EntryDefinition(ApacheHttpClientAdvice.class, true);

		// \ Method method = Whitebox.getMethod(Object.class, "<<interceptingMethod>>");

		// test before method
		// ApacheHttpClientAdvice.before();

		// test after method
		// ApacheHttpClientAdvice.after();
	}

	public static void main(String[] args) throws Throwable {
		System.setProperty("probe.output", SysOutOutput.class.getName());
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println( //
				"\n\tClass.loader=" + Remora.class.getClassLoader() //
						+ "\n\tJava.version=" + System.getProperty("java.version") //
						+ "\n\tJava.vendor=" + System.getProperty("java.vendor") //
						+ "\n\tJava.home=" + System.getProperty("java.home") //
						+ "\n\tJava.heap=" + Runtime.getRuntime().maxMemory() //
						+ "\n\tOS.name=" + System.getProperty("os.name") //
						+ "\n\tOS.version=" + System.getProperty("os.version") //
						+ "\n\tOS.arch=" + System.getProperty("os.arch") //
						+ "\n\tOS.cpus=" + Runtime.getRuntime().availableProcessors());

		// new JustATest().instrumentedMethod("http://www.google.com", "Argument");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://www.google.com");
		CloseableHttpResponse response = httpClient.execute(request);
		System.out.println(EntityUtils.toString(response.getEntity()));

		Thread.sleep(3000);

		httpClient.close();
	}

}