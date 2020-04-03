package com.jkoolcloud.remora.advices;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;
import org.takes.tk.TkOnce;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.AdminReporter;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.adviceListeners.CountingAdviceListener;
import com.jkoolcloud.remora.takes.*;

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
	protected static CountingAdviceListener adviceListener;

	@Override
	public void install(Instrumentation instrumentation) {
		logger = Logger.tag(ADVICE_NAME);
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		AvailableInetSocketAddress[] availableInetSocketAddress = { null };

		scheduledExecutorService.schedule(() -> {
			try {
				availableInetSocketAddress[0] = new AvailableInetSocketAddress(port);
				InetSocketAddress inetSocketAddress = availableInetSocketAddress[0].getInetSocketAddress();
				startHttpServer(inetSocketAddress);
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
					System.getProperty(Remora.REMORA_VM_IDENTIFICATION), logger);
			logger.info("Admin reporter initialised, wil invoke every {} seconds", reporterSchedule);
			ScheduledExecutorService adminServiceQuery = Executors.newScheduledThreadPool(1);
			adminServiceQuery.scheduleAtFixedRate(() -> adminReporter.report(), 0, reporterSchedule, TimeUnit.SECONDS);
		} else {
			logger.info("Admin reporter will be not initialised, admin reporter endpoint not set");
		}

		adviceListener = new CountingAdviceListener();
		BaseTransformers.registerListener(adviceListener);

	}

	protected static void startHttpServer(InetSocketAddress address) throws IOException {
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				new FtBasic(//
						new TkFork(//
								new FkRegex("/", new TkAdviceList()), //
								new FkRegex("/change", //
										new TkFork(//
												new FkMethods("POST", new TkOnce(new TkChange(logger))))),
								new FkRegex("/statistics/(?<advice>[^/]+)", new TKStatistics()), //
								new FkRegex("/queueStatistics", new TkQueueStatistics()), //
								new FkRegex("/threadDump", new TkThreadDump())//

				), //

						address.getPort()).start(Exit.NEVER);
			} catch (IOException e) {
				logger.error(e);
			}
		});
	}

	@Override
	public String getName() {
		return ADVICE_NAME;
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

	public static CountingAdviceListener getAdviceListener() {
		return adviceListener;
	}

}
