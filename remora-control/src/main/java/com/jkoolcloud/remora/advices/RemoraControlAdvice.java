package com.jkoolcloud.remora.advices;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.jkoolcloud.remora.AdviceRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

//import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;

public class RemoraControlAdvice implements RemoraAdvice {

	public static final String ADVICE_NAME = "RemoraControlAdvice";

	// @RemoraConfig.Configurable
	// public static boolean load = true;
	// @RemoraConfig.Configurable
	// public static boolean logging = false;
	public int port = 7366;
	// public HttpContextBuilder contextBuilder;

	@Override
	public void install(Instrumentation instrumentation) {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.schedule(() -> {
			try {
				HttpServer httpServer = null;

				httpServer = HttpServer.create(new InetSocketAddress(port), 10);
				httpServer.createContext("/", new GetCapabilitiesHandler());
				httpServer.createContext("/change", new PropertiesChangeHandler());
				httpServer.setExecutor(null);
				// contextBuilder = new HttpContextBuilder();
				// contextBuilder.getDeployment().getActualResourceClasses().add(RestResource.class);
				// HttpContext context = contextBuilder.bind(httpServer);
				// context.getAttributes().put("some.config.info", "42");
				httpServer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 3, TimeUnit.MINUTES);
	}

	private void destroy(HttpServer httpServer) {
		// contextBuilder.cleanup();
		httpServer.stop(0);
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	static class GetCapabilitiesHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			StringBuilder response = formatResponse();

			response.append("]");

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(String.valueOf(response).getBytes());
			os.close();
		}
	}

	@NotNull
	protected static StringBuilder formatResponse() {
		StringBuilder response = new StringBuilder();
		response.append("[");
		List<RemoraAdvice> registeredAdvices = AdviceRegistry.INSTANCE.getRegisteredAdvices();
		for (int i = 0; i < registeredAdvices.size(); i++) {
			RemoraAdvice advice = registeredAdvices.get(i);
			response.append("\t{\n");
			response.append("\tadviceName: ");
			response.append(advice.getClass().getSimpleName());
			response.append("\n");
			List<String> configurableFields = AdviceRegistry.getConfigurableFields(advice);
			Map<String, String> fieldsAndValues = AdviceRegistry.mapToCurrentValues(advice, configurableFields);
			response.append("\tproperties: {\n");
			response.append(
					fieldsAndValues.entrySet().stream().map(entry -> "\t\t" + entry.getKey() + " : " + entry.getValue())
							.collect(Collectors.joining(",\n")));

			response.append("\n\t}");
			if (i != registeredAdvices.size() - 1) {
				response.append(",\n");
			} else {
				response.append("\n");
			}
		}
		response.append("}\n");
		return response;
	}

	static class PropertiesChangeHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			t.getRequestURI();
			String response = "This is the response";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

}
