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

package com.jkoolcloud.remora;

import java.io.File;
import java.io.FileFilter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.core.EntryDefinition;
import com.jkoolcloud.remora.core.output.OutputManager;
import com.jkoolcloud.remora.core.utils.RemoraClassLoader;

public class Remora {

	public static TaggedLogger logger;

	public static final boolean DEBUG_BOOT_LOADER = true;
	public static final String MODULES_DIR = "/modules";
	public static final String REMORA_PATH = "remora.path";
	public static final String MAIN_LOG = "/log/" + "remora%u.log";
	private static URLClassLoader bootLoader;

	public Remora() {
		logger.info("App loaded with classloader: " + getClass().getClassLoader());
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting from main");
		System.out.println("This application intended to be used as javaAgent.");
	}

	public static void premain(String options, Instrumentation inst) throws Exception {
		if (System.getProperty(REMORA_PATH) == null) {
			System.setProperty(REMORA_PATH, options);
		}

		String baseRemoraDir = System.getProperty(Remora.REMORA_PATH);

		// logger.info("Starting from premain; classloader: " + Remora.class.getClassLoader());

		inst.appendToBootstrapClassLoaderSearch(new JarFile(baseRemoraDir + "/" + "remora.jar"));
		bootLoader = new RemoraClassLoader(findJars(baseRemoraDir + MODULES_DIR), Remora.class.getClassLoader(), inst);
		// logger.info("Initializing classloader: " + bootLoader);
		Class<?> appClass = bootLoader.loadClass("com.jkoolcloud.remora.RemoraInit");
		Object instance = appClass.newInstance();
		// logger.info("Initializing agent class: " + appClass);
		Method initializeAdvices = appClass.getMethod("initializeAdvices", Instrumentation.class, ClassLoader.class);
		// logger.info("Initializing advices: " + appClass);
		initializeAdvices.invoke(instance, inst, bootLoader);
		logger = Logger.tag("INIT");
		EntryDefinition.setVmIdentification(System.getProperty("remoraVMIdentification", getDefaultVM()));
		RemoraConfig remoraConfig = RemoraConfig.INSTANCE; // Load output and config manager by Bootstarp classloader;
		OutputManager outputManager = OutputManager.INSTANCE; //
	}

	private static String getDefaultVM() {
		String processName;
		try {
			processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		} catch (Exception e) {
			processName = "NA";
		}
		return processName;
	}

	public static URL[] findJars(String location) throws MalformedURLException {
		File moduleFolder = new File(location);

		if (moduleFolder.exists()) {

			File[] modulesFiles = moduleFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String name = pathname.getName();
					return name.endsWith(".jar") || name.endsWith(".zip");
				}
			});
			if (modulesFiles == null) {
				logger.error("No modules found");
				return null;
			}
			List<File> files = Arrays.asList(modulesFiles);

			URL[] uris = files.stream().map(file -> {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					return null;
				}
			}).peek(file -> logFoundClasspathEntry(file)).collect(Collectors.toList()).toArray(new URL[files.size()]);

			return uris;
		} else {
			logger.error(
					"Module dir is not existing; Check java agent parameters; --javaagent:remora.jar=parameters. Parameters should point to the directory remora exists");
			return null;
		}
	}

	private static void logFoundClasspathEntry(URL file) {
		if (logger == null) {
			System.out.println("Found: " + file);
		} else {
			logger.info("Found: " + file);
		}
	}

}
