
/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.jkoolcloud.testHarness;

import static java.text.MessageFormat.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jkoolcloud.testHarness.endpoints.WebsocketEchoEndpoint;
import com.jkoolcloud.testHarness.harnesses.*;

public class Setup extends HttpServlet {

	public static final String EXECUTOR_SERVICES = "executorServices";
	private static Class<? extends Harness>[] harnesses = new Class[] { ApacheHttpClientHarness.class, SQLHarness.class,
			MQReceiveHarness.class, MQSendHarness.class, WebsocketSendHarness.class, KafkaConsumerHarness.class,
			KafkaProducerHarness.class };
	private static HashMap<ExecutorService, Harness> runningHarnesses = new HashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		config.getServletContext().setAttribute(EXECUTOR_SERVICES, new HashMap<ExecutorService, Collection>());
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();

		if (req.getRequestURI().endsWith("/js/script.js")) {
			resp.setContentType("application/javascript");
			resp.getWriter().write(getString(req.getServletContext().getResourceAsStream("/static/script/script.js")));
			return;
		}

		if (req.getRequestURI().endsWith("/css/style.css")) {
			resp.setContentType("text/css");
			resp.getWriter().write(getString(req.getServletContext().getResourceAsStream("/static/css/style.css")));
			return;
		}
		out.println("<html>");
		out.println("<head>");
		outputStyle(out, req.getContextPath());
		outputScript(out, req.getContextPath());
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>Test harnesses</h1>");

		out.println("<table>");
		for (Class<? extends Harness> harnessClass : harnesses) {
			out.println("<form action=\"" + req.getContextPath() + "\" method=\"post\">");

			out.println("<tr><td>");
			out.println(harnessClass.getSimpleName());
			out.println("</td>");
			out.println("<td>");
			printScheduleSelection(out, harnessClass);
			out.println("</td>");
			out.println("<td>");
			printConfigurables(out, harnessClass);
			out.println("</td>");
			out.println("<td>");
			out.println("<input type=\"submit\" value=\"+\"><br>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</form>");
		}
		out.println("</table>");
		out.println("<table>");
		printRunningExecutors(out, req);

		out.println("</form>");
		out.println("</table>");

		out.println("</body>");
		out.println("</html>");

	}

	private void printRunningExecutors(PrintWriter out, HttpServletRequest req) {

		out.println("<h1>Running executors</h1>");
		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) req
				.getServletContext().getAttribute(EXECUTOR_SERVICES);
		for (ExecutorService service : executorServices.keySet()) {

			out.println("<tr><td>");
			out.println(runningHarnesses.get(service));
			out.println("</td>");

			out.println("<td>");
			out.println(service.isTerminated() ? "Terminated" : "Running");
			out.println("</td>");

			out.println("<td>");
			out.println(service.isShutdown() ? "Shutdown" : "Running");
			out.println("</td>");

			out.println("<td>");
			if (service instanceof ThreadPoolExecutor) {
				out.println(((ThreadPoolExecutor) service).getQueue().size());
			}
			out.println("</td>");

			out.println("<td>");
			if (service instanceof ThreadPoolExecutor) {
				out.println(((ThreadPoolExecutor) service).getCompletedTaskCount());
			}
			out.println("</td>");

			out.println("<td>");
			for (Object o : executorServices.get(service)) {
				if (o instanceof Future) {
					if (((Future) o).isDone()) {
						try {
							out.println(((Future) o).get());
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							out.println(format("Exception: {0}, {1}", e.getClass().getSimpleName(), e.getMessage()));
						}
					} else {
						out.println("Scheduled..");
					}

				} else {
					out.println(o);
				}
				out.println("<br>");
			}
			out.println("</td>");

			out.println("<td>");
			out.println(format("<form action=\"{0}\" method=\"POST\">", req.getContextPath()));
			out.println("<input type='submit' value='Remove'>");
			out.println(format("<input type='hidden' name='service' value=\"{0,number,#}\">", service.hashCode()));
			out.println("</form>");

			out.println("</td>");

			out.println("</tr>");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) req
				.getServletContext().getAttribute(EXECUTOR_SERVICES);
		Integer serviceHash = Integer.valueOf(req.getParameter("service"));

		ExecutorService toRemove = null;
		for (ExecutorService service : executorServices.keySet()) {
			if (service.hashCode() == serviceHash.intValue()) {
				service.shutdown();
				toRemove = service;
				break;
			}
		}
		if (toRemove != null) {
			executorServices.remove(toRemove);
			runningHarnesses.remove(toRemove);
		}

		resp.sendRedirect(req.getContextPath());
	}

	private void printScheduleSelection(PrintWriter out, Class<? extends Harness> harnessClass) {
		out.println(format("<input size='30' type='hidden' name=\"class\" value=\"{0}\">", harnessClass.getName()));
		out.print("<select name='scheduleType'>");
		out.println("<option name='NumberOfTimes' value='NumberOfTimes'>Number Of Times</option>");
		out.println("<option name='Scheduled' value='Scheduled' selected=\"true\">Scheduled</option>");
		out.print("</select>");
		out.print("<label for='scheduleValue'># of times, or ms</label>");
		out.println(format("<input size='30' name='scheduleValue' value='500'>"));
		out.print("<label for='threads'>Threads</label>");
		out.println(format("<input size='30' name='threads' value='1'>"));
	}

	protected static void printConfigurables(PrintWriter out, Class<? extends Harness> harnessClass) {

		out.println("<div>");
		Class workingClass = harnessClass;

		Harness tempObj = null;
		try {
			tempObj = harnessClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		while (!workingClass.equals(Object.class)) {
			INNER: for (Field field : workingClass.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Configurable.class)) {
					continue INNER;
				}
				out.println("<div>");
				field.getName();
				try {

					if (field.getType().isEnum()) {
						out.println(format("<div>{0}</div>", field.getName()));
						out.print(format("<select name={0}>", field.getName()));
						for (String constant : getNames((Class<? extends Enum<?>>) field.getType())) {
							out.println(format("<option name='{0}' value='{1}' selected='{2}'>{3}</option>", constant,
									constant, constant.equalsIgnoreCase(((Enum) field.get(tempObj)).name()), constant));
						}
						out.print("</select>");
					} else {
						out.println(format("<div>{0}</div>", field.getName()));

						out.println(format("<input size='30' name=\"{0}\" value=\"{1}\">", field.getName(),
								field.get(tempObj) == null ? "" : field.get(tempObj)));
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				out.println("</div>");

			}
			workingClass = workingClass.getSuperclass();
		}
		out.println("</div>");
	}

	public static String[] getNames(Class<? extends Enum<?>> e) {
		return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
	}

	public static Enum getEnumValue(Class<? extends Enum<?>> e, String name) {
		Enum<?>[] enumConstants = e.getEnumConstants();
		Enum result = enumConstants[0];
		for (Enum enumValue : enumConstants) {
			if (enumValue.name().equalsIgnoreCase(name)) {
				result = enumValue;
				break;
			}
		}

		return result;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String service = req.getParameter("service");
		if (service != null) {
			doDelete(req, resp);
		} else {
			createNewHarness(req, resp);
		}
	}

	private void createNewHarness(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		boolean exception = false;
		String scheduleType = req.getParameter("scheduleType");
		Integer numberOfThread = Integer.valueOf((String) req.getParameter("threads"));
		Integer scheduleValue = Integer.valueOf((String) req.getParameter("scheduleValue"));
		String className = req.getParameter("class");

		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) req
				.getServletContext().getAttribute(EXECUTOR_SERVICES);

		try {
			Harness harness = (Harness) Class.forName(className).newInstance();
			setup(harness, req.getParameterMap());

			switch (scheduleType) {
			case "Scheduled": {
				ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(numberOfThread);

				PeriodicRunnableHarness command = new PeriodicRunnableHarness(harness);
				ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(command, 0,
						scheduleValue, TimeUnit.MILLISECONDS);
				executorServices.put(scheduledExecutorService, command);
				runningHarnesses.put(scheduledExecutorService, harness);

			}
				break;
			default:
			case "NumberOfTimes": {
				ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
				ArrayList results = new ArrayList();
				for (int i = 0; i <= scheduleValue; i++) {
					Future<HarnessResult> submit = executorService.submit(harness);
					results.add(submit);
				}
				executorServices.put(executorService, results);
				runningHarnesses.put(executorService, harness);

			}
				break;
			}

		} catch (Exception e) {
			exception = true;
			e.printStackTrace(resp.getWriter());
		}
		if (!exception) {
			resp.sendRedirect(req.getContextPath());
		}
	}

	@Override
	public void destroy() {
		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) getServletContext()
				.getAttribute(EXECUTOR_SERVICES);
		if (executorServices != null) {
			executorServices.keySet().forEach(executorService -> executorService.shutdown());
		}
		super.destroy();
	}

	private void setup(Harness harness, Map<String, String[]> parameterMap) throws Exception {

		Class workingClass = harness.getClass();
		while (!workingClass.equals(Object.class)) {
			for (Field field : workingClass.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Configurable.class)) {
					continue;
				}
				String value = parameterMap.get(field.getName())[0];
				Class<?> type = field.getType();
				try {
					if (type.equals(String.class)) {
						field.set(harness, value);
					}
					if (type.equals(Integer.class)) {
						field.set(harness, Integer.valueOf(value.replaceAll("(\\h*)|(\\h*$)", "")));
					}
					if (type.equals(Long.class)) {
						field.set(harness, Long.valueOf(value.replaceAll("(\\h*)|(\\h*$)", "")));
					}
					if (type.isEnum()) {
						field.set(harness, getEnumValue((Class<? extends Enum<?>>) type, value));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			workingClass = workingClass.getSuperclass();
		}
		harness.setup();
	}

	private static void outputStyle(PrintWriter out, String cp) {
		out.println("<link rel=\"stylesheet\" href=\"" + cp + "/static/css/style.css\">");
	}

	private static void outputScript(PrintWriter out, String cp) {
		out.println("<script src=\"" + cp + "/static/js/script.js\"></script>");
	}

	private static String getString(InputStream inputStream) throws IOException {
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			return result.toString();
		} catch (Exception e) {
			return "N/A";
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();
		WebsocketEchoEndpoint endpoint = new WebsocketEchoEndpoint();
	}
}
