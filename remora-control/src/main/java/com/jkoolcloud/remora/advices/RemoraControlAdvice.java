package com.jkoolcloud.remora.advices;

import static java.text.MessageFormat.format;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;
import org.takes.rs.RsText;
import org.takes.tk.TkOnce;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.RemoraConfig;

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
				startHttpServer2(new AvailableInetSocketAddress(port).getInetSocketAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 3, TimeUnit.MINUTES);
	}

	protected static void startHttpServer2(InetSocketAddress address) throws IOException {
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				new FtBasic(//
						new TkFork(//
								new FkRegex("/", formatResponse().toString()), //
								new FkRegex("/change", //
										new TkFork(//
												new FkMethods("POST", new TkOnce(new Take() {
													@Override
													public Response act(Request request) throws Exception {
														String body = getBody(request.body());
														String adviceName = getValueForKey("advice", body);
														String property = getValueForKey("property", body);
														String value = getValueForKey("value", body);

														return new RsText(applyChanges(adviceName, property, value));
													}
												}))))), //

						address.getPort()).start(Exit.NEVER);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static String applyChanges(String adviceName, String property, String value) {
		try {
			RemoraAdvice adviceByName = AdviceRegistry.INSTANCE.getAdviceByName(adviceName);
			Field field = adviceByName.getClass().getField(property);
			if (!field.isAnnotationPresent(RemoraConfig.Configurable.class)) {
				throw new NoSuchFieldException();
			}
			Object appliedValue = RemoraConfig.getAppliedValue(field, value);
			field.set(null, appliedValue);
			return "OK";
		} catch (ClassNotFoundException e) {
			return "No such advice";
		} catch (IllegalAccessException e) {
			return "Cant change advices:" + property + " property: " + property;
		} catch (NoSuchFieldException e) {
			return "No such property";
		}
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
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

	@NotNull
	protected static String getBody(InputStream t) throws IOException {
		StringBuilder bodySB = new StringBuilder();
		try (InputStreamReader reader = new InputStreamReader(t)) {
			char[] buffer = new char[256];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				bodySB.append(buffer, 0, read);
			}
		}
		return bodySB.toString();
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

	public static class AdminReporter {

		private final int port;
		private final String name;
		private String REPORT_MESSAGE_TEMPLATE = "{\n" + "\t\"adress\": \"{}}\",\n" + "\t\"port\": {}},\n"
				+ "\t\"vmIdentification\": \"{}}\"\n" + "}";
		private final String localAddress;
		private URL url;

		public AdminReporter(String address, int port, String name) throws IOException {
			url = new URL(address);

			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(url.getHost(), url.getPort()));
			InetAddress localAddress1 = socket.getLocalAddress();
			localAddress = localAddress1.getHostAddress();

			this.port = port;
			this.name = name;
			url = url;
			report();

		}

		protected boolean report() throws IOException {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			try (OutputStream out = con.getOutputStream()) {
				byte[] input = format(REPORT_MESSAGE_TEMPLATE, localAddress, port, name).getBytes();
				out.write(input, 0, input.length);
				out.flush();
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}
			return true;
		}
	}
}
