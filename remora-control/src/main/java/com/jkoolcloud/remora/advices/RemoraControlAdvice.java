package com.jkoolcloud.remora.advices;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

				httpServer = HttpServer.create(new AvailableInetSocketAddress(port).getInetSocketAddress(), 10);
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
			response.append("\t\"adviceName\": ");
			response.append("\"");
			response.append(advice.getClass().getSimpleName());
			response.append("\"");
			response.append(",\n");
			List<String> configurableFields = AdviceRegistry.getConfigurableFields(advice);
			Map<String, String> fieldsAndValues = AdviceRegistry.mapToCurrentValues(advice, configurableFields);
			response.append("\t\"properties\": {\n");
			response.append(fieldsAndValues.entrySet().stream()
					.map(entry -> "\t\t\"" + entry.getKey() + "\" : \"" + entry.getValue() + "\"")
					.collect(Collectors.joining(",\n")));

			response.append("\n\t}}");
			if (i != registeredAdvices.size() - 1) {
				response.append(",\n");
			} else {
				response.append("\n");
			}
		}
		response.append("]\n");
		return response;
	}

	static class PropertiesChangeHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			if (!t.getRequestMethod().equals("POST")) {
				generateError(t, "Method not allowed", 405);
			}

			if (Optional.of(t.getRequestHeaders().get("Content-length")).get().get(0).length() > 2) {
				generateError(t, "Oversize request", 413);
			}
			StringBuilder bodySB = new StringBuilder();
			try (InputStreamReader reader = new InputStreamReader(t.getRequestBody())) {
				char[] buffer = new char[256];
				int read;
				while ((read = reader.read(buffer)) != -1) {
					bodySB.append(buffer, 0, read);
				}
			}
			String body = bodySB.toString();

			try {
				String adviceName = getValueForKey("advice", body);
			} catch (ParseException e) {
				generateError(t, e.getMessage(), 500);
			}

			String response = "OK";

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}

	}

	protected static String getValueForKey(String key, String body) throws ParseException {
		Pattern pattern = Pattern.compile(String.format("\"%s\"\\s*:\\s*\"((?=[ -~])[^\"]+)\"", key));
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new ParseException(format("Cannot extract {} from \n {}", key, body), 0);
		}

	}

	protected static void generateError(HttpExchange t, String message, int code) throws IOException {
		t.sendResponseHeaders(code, message.length());
		OutputStream responseBody = t.getResponseBody();
		responseBody.write(message.getBytes());
	}

	public static class AvailableInetSocketAddress {
		InetSocketAddress delegate;

		public AvailableInetSocketAddress(int port) {
			do {
				if (!isLocalPortFree(port)) {
					port++;
					continue;
				}
				delegate = new InetSocketAddress(port);

			} while (delegate == null);
		}

		private boolean isLocalPortFree(int port) {
			try {
				new ServerSocket(port).close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public InetSocketAddress getInetSocketAddress() {
			return delegate;
		}
	}
}
