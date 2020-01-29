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

package com.jkoolcloud.remora.core.utils;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;

public class RemoraClassLoader extends URLClassLoader {
	// TaggedLogger logger = Logger.tag("INFO");

	public RemoraClassLoader(URL[] urls, ClassLoader parent, Instrumentation inst) {
		super(urls, parent);
		for (URL url : urls) {
			try {
				// logger.info("Appending boot classloader with: " + url);
				inst.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		RemoraConfig.INSTANCE.classLoader = this;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;
		Throwable error = null;
		try {
			clazz = super.findClass(name);
			return clazz;
		} catch (ClassNotFoundException ce) {
			error = ce;
			throw ce;
		} finally {
			if (Remora.DEBUG_BOOT_LOADER) {
				// logger.info(
				// this + " findClass(" + name + "), loader=" + (clazz != null ? clazz.getClassLoader() : null));
				if (error != null) {
					// logger.info("Exception: {} {} \n {}", "RemoraClassLoader", "finClass", error));
				}
			}
		}
	}
}
