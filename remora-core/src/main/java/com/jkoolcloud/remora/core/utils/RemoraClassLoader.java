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

package com.jkoolcloud.remora.core.utils;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;

public class RemoraClassLoader extends URLClassLoader {
	// USING LOGGER at classloader will cause java.lang.ClassCircularityError
	// TaggedLogger logger = Logger.tag("INFO");

	public RemoraClassLoader(URL[] urls, ClassLoader parent, Instrumentation inst) {
		super(urls, parent);
		for (URL url : urls) {
			try {
				inst.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} catch (IOException e) {
				System.err.println("Remora classloader can't load " + url);
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
