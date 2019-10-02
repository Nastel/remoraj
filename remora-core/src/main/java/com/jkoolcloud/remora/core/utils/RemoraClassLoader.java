package com.jkoolcloud.remora.core.utils;

import com.jkoolcloud.remora.Remora;

import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;

public class RemoraClassLoader extends  URLClassLoader {
    public RemoraClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
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
                PrintStream out = error == null? System.out: System.err;
                out.println(this + " findClass(" + name + "), loader="
                        + (clazz != null ? clazz.getClassLoader() : null));
                if (error != null)
                    error.printStackTrace();
            }
        }
    }
}