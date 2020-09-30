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

package com.jkoolcloud.remora.advices;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;
import org.takes.tk.TkOnce;
import org.tinylog.Level;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdminReporter;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.adviceListeners.CountingAdviceListener;
import com.jkoolcloud.remora.adviceListeners.TimingAdviceListener;
import com.jkoolcloud.remora.takes.*;

//import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;

public class RemoraControlAdvice implements RemoraAdvice, Loggable {

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
	@RemoraConfig.Configurable
	public static int serviceDelay = 240;
	@RemoraConfig.Configurable
	public static String heapDumpPath = System.getProperty(Remora.REMORA_PATH, ".") + "/dumps/";
	private Level logLevel = Level.OFF;

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		AvailableInetSocketAddress[] availableInetSocketAddress = { null };

		logger.info("Scheduling http server after {} {}", serviceDelay, TimeUnit.SECONDS);
		scheduledExecutorService.schedule(() -> {
			try {
				availableInetSocketAddress[0] = new AvailableInetSocketAddress(port);
				InetSocketAddress inetSocketAddress = availableInetSocketAddress[0].getInetSocketAddress();
				startHttpServer(inetSocketAddress);
				logger.info("Initialized Remora control instance on {}, port: {}", inetSocketAddress.getHostName(),
						inetSocketAddress.getPort());
			} catch (IOException e) {
				logger.error("Cannot initialize remora control instance. \n {}", e);
			}
		}, serviceDelay, TimeUnit.SECONDS);

		if (adminURL != null) {
			AdminReporter adminReporter = new AdminReporter(adminURL,
					availableInetSocketAddress[0] == null ? 0
							: availableInetSocketAddress[0].getInetSocketAddress().getPort(),
					System.getProperty(Remora.REMORA_VM_IDENTIFICATION), logger);
			logger.info("Admin reporter initialized, wil invoke every {} seconds", reporterSchedule);
			ScheduledExecutorService adminServiceQuery = Executors.newScheduledThreadPool(1);
			adminServiceQuery.scheduleAtFixedRate(() -> adminReporter.report(), 0, reporterSchedule, TimeUnit.SECONDS);
		} else {
			logger.info(
					"Admin reporter will be not initialized, ctx.interceptorInstance, admin reporter endpoint not set");
		}

		BaseTransformers.registerListener(CountingAdviceListener.class);
		BaseTransformers.registerListener(TimingAdviceListener.class);

	}

	protected static void startHttpServer(InetSocketAddress address) throws IOException {
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				Fork[] remoraControlEndpoints = { new FkRegex("/", new TkAdviceList()), //
						new FkRegex("/change", //
								new TkFork(//
										new FkMethods("POST", new TkOnce(new TkChange(logger))))),
						new FkRegex("/stats/(?<advice>[^/]+)", new TkFork( //
								new FkMethods("GET", new TKStatistics()),
								new FkMethods("DELETE", new TkStatisticsDelete()))), //
						new FkRegex("/queueStats", new TkQueueStatistics()), //
						new FkRegex("/threadDump", new TkThreadDump()), //
						new FkRegex("/gcInfo", new TkGCInfo()), //
						new FkRegex("/sysInfo", new TkSystemInfo()), //
						new FkRegex("/filters", new TkFork(new FkMethods("GET", new TKFilters()), //
								new FkMethods("POST", new TkNewFilter(logger)))), //
						new FkRegex("/heapDump", new TkHeapDump(heapDumpPath)) };

				List<Fork> endpoints = new ArrayList<>(Arrays.asList(remoraControlEndpoints));

				ServiceLoader<PluginTake> pluginEndpoints = ServiceLoader.load(PluginTake.class,
						Remora.getClassLoader());
				pluginEndpoints.forEach(pluginEndpoint -> {
					endpoints.add(new FkRegex(pluginEndpoint.getEnpointPath(), new TkCorsAllowAll(pluginEndpoint)));
					logger.info("Added custom endpoint {} for path {}", pluginEndpoint.getClass().getSimpleName(),
							pluginEndpoint.getEnpointPath());
				});

				new FtBasic(new TkFork(endpoints),

						address.getPort()).start(Exit.NEVER);

			} catch (Exception e) {
				logger.error(e);
			}
		});
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
	}

	@Override
	public Level getLogLevel() {
		return logLevel;
	}

	@Override
	public TaggedLogger getLogger() {
		return logger;
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

}
