
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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jkoolcloud.testHarness.harnesses.*;

public class Setup extends HttpServlet {

	public static final String EXECUTOR_SERVICES = "executorServices";
	public static final int RESULT_SIZE = 100;
	private Class<? extends Harness>[] harnesses = new Class[] { ApacheHttpClientHarness.class, SQLHarness.class };
	private ArrayList<Harness> runningHarnesses = new ArrayList<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		config.getServletContext().setAttribute(EXECUTOR_SERVICES, new HashMap<ExecutorService, Collection>());
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<head>");

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

		out.println("<form action=\"" + req.getContextPath() + "\" method=\"delete\">");
		out.println("<table>");

		printRunningExecutors(out, req.getServletContext());

		out.println("</form>");
		out.println("</table>");

		out.println("</body>");
		out.println("</html>");

	}

	private void printRunningExecutors(PrintWriter out, ServletContext servletContext) {
		out.println("<h1>Running executors</h1>");
		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) servletContext
				.getAttribute(EXECUTOR_SERVICES);
		for (ExecutorService service : executorServices.keySet()) {

			// out.println("<tr><td>");
			// out.println(service.);
			// out.println("</td>");

			out.println("<tr><td>");
			out.println(service.isTerminated());
			out.println("</td>");

			out.println("<td>");
			out.println(service.isShutdown());
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
				out.println(o);
				out.println("<br>");
			}
			out.println("</td>");

			out.println("</tr>");
		}
	}

	private void printScheduleSelection(PrintWriter out, Class<? extends Harness> harnessClass) {
		out.println(format("<input size='30' type='hidden' name=\"class\" value=\"{0}\">", harnessClass.getName()));
		out.print("<select name='scheduleType'>");
		out.println("<option name='NumberOfTimes' value='NumberOfTimes'>Number Of Times</option>");
		out.println("<option name='Scheduled' value='Scheduled'>Scheduled</option>");
		out.print("</select>");
		out.println(format("<input size='30' name='scheduleValue' value='1'>"));
		out.println(format("<input size='30' name='threads' value='1'>"));
	}

	private void printConfigurables(PrintWriter out, Class<? extends Harness> harnessClass) {

		out.println("<div>");
		for (Field field : harnessClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Configurable.class)) {
				continue;
			}
			out.println("<div>");

			field.getName();
			try {
				Harness tempObj = harnessClass.newInstance();

				if (field.getType().isEnum()) {
					out.print(format("<select name={0}>", field.getName()));
					for (String constant : getNames((Class<? extends Enum<?>>) field.getType())) {
						out.println(format("<option name='{0}' value='{1}' selected='{2}'>{3}</option>", constant,
								constant, constant.equalsIgnoreCase(((Enum) field.get(tempObj)).name()), constant));
					}
					out.print("</select>");
				} else {
					out.println(format("<div>{0}</div>", field.getName()));

					out.println(format("<input size='30' name=\"{0}\" value=\"{1}\">", field.getName(),
							field.get(tempObj)));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			out.println("</div>");
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
		String scheduleType = req.getParameter("scheduleType");
		Integer numberOfThread = Integer.valueOf(req.getParameter("threads"));
		Integer scheduleValue = Integer.valueOf(req.getParameter("scheduleValue"));
		String className = req.getParameter("class");

		HashMap<ExecutorService, Collection> executorServices = (HashMap<ExecutorService, Collection>) req
				.getServletContext().getAttribute(EXECUTOR_SERVICES);

		try {
			Harness harness = (Harness) Class.forName(className).newInstance();
			setup(harness, req.getParameterMap());
			runningHarnesses.add(harness);
			switch (scheduleType) {
			case "Scheduled": {
				ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(numberOfThread);
				ConcurrentLinkedQueue<HarnessResult> results = new ConcurrentLinkedQueue<>();

				ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						try {
							HarnessResult result = harness.call();
							results.offer(result);
						} catch (Exception e) {
							System.out.println();
						}
					}
				}, 0, scheduleValue, TimeUnit.MILLISECONDS);
				executorServices.put(scheduledExecutorService, results);

			}
				break;
			default:
			case "NumberOfTimes": {
				ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
				ArrayList results = new ArrayList();
				for (int i = 0; i <= scheduleValue; i++) {
					results.add(executorService.submit(harness));
				}
				executorServices.put(executorService, results);
			}
				break;
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		doGet(req, resp);
	}

	private void setup(Harness harness, Map<String, String[]> parameterMap) {
		try {
			for (Field field : harness.getClass().getDeclaredFields()) {
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

				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			System.out.println();
		}
		harness.setup();
	}

}
