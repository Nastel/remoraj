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
