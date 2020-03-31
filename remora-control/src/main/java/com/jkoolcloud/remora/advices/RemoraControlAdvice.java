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
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdviceRegistry;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;

//import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;

public class RemoraControlAdvice implements RemoraAdvice {

	public static final String ADVICE_NAME = "RemoraControlAdvice";
	protected static TaggedLogger logger;
	// @RemoraConfig.Configurable
	// public static boolean load = true;
	// @RemoraConfig.Configurable
	// public static boolean logging = false;
	@RemoraConfig.Configurable
	public static int port = 7366;
	@RemoraConfig.Configurable
	public static String adminURL = null;
	@RemoraConfig.Configurable
	public static int reporterSchedule = 300;

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		AvailableInetSocketAddress[] availableInetSocketAddress = { null };

		scheduledExecutorService.schedule(() -> {
			try {
				availableInetSocketAddress[0] = new AvailableInetSocketAddress(port);
				InetSocketAddress inetSocketAddress = availableInetSocketAddress[0].getInetSocketAddress();
				startHttpServer2(inetSocketAddress);
				logger.info("Initialised Remora control instance on {}, port: {}", inetSocketAddress.getHostName(),
						inetSocketAddress.getPort());
			} catch (IOException e) {
				logger.error("Cannot initialize remora control instance. \n {}", e);
			}
		}, 3, TimeUnit.MINUTES);

		if (adminURL != null) {
			AdminReporter adminReporter = new AdminReporter(adminURL,
					availableInetSocketAddress[0] == null ? 0
							: availableInetSocketAddress[0].getInetSocketAddress().getPort(),
					System.getProperty(Remora.REMORA_VM_IDENTIFICATION));
			logger.info("Admin reporter initialised, wil invoke every {} seconds", reporterSchedule);
			ScheduledExecutorService adminServiceQuery = Executors.newScheduledThreadPool(1);
			adminServiceQuery.scheduleAtFixedRate(() -> adminReporter.report(), 0, reporterSchedule, TimeUnit.SECONDS);
		} else {
			logger.info("Admin reporter will be not initialised, admin reporter endpoint not set");
		}

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
				logger.error(e);
			}
		});
	}

	private static String applyChanges(String adviceName, String property, String value) {
		try {
			logger.info("Ivoked remote request for \"{}\" property \"{}\" change. New value: {} ", adviceName, property,
					value);
			RemoraAdvice adviceByName = AdviceRegistry.INSTANCE.getAdviceByName(adviceName);
			Field field = adviceByName.getClass().getField(property);
			if (!field.isAnnotationPresent(RemoraConfig.Configurable.class)) {
				throw new NoSuchFieldException();
			}
			Object appliedValue = RemoraConfig.getAppliedValue(field, value);
			field.set(null, appliedValue);
			return "OK";
		} catch (ClassNotFoundException e) {
			String m = "No such advice";
			logger.error("\t " + m + "\n {}", e);

			return m;
		} catch (IllegalAccessException e) {
			String m = "Cant change advices:" + property + " property: " + property;
			logger.error("\t " + m + "\n {}", e);
			return m;
		} catch (NoSuchFieldException e) {
			String m = "No such property";
			logger.error("\t " + m + "\n {}", e);
			return m;
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
					logger.info("Port {} is used, setting next", port);
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
		private String localAddress = null;
		private URL url;

		public AdminReporter(String address, int port, String name) {
			this.port = port;
			this.name = name;

			try {
				url = new URL(address);

				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(url.getHost(), url.getPort()));
				InetAddress localAddress1 = socket.getLocalAddress();
				localAddress = localAddress1.getHostAddress();

				url = url;
			} catch (Exception e) {
				logger.error("Cannot initialize admin reporter: \n {}", e);
			}

		}

		protected boolean report() {
			try {
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
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}
}
