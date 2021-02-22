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

import com.jkoolcloud.remora.advices.BaseTransformers;
import org.tinylog.Level;
import org.tinylog.core.TinylogLoggingProvider;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.ContextProvider;
import org.tinylog.provider.LoggingProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemoraLoggingProvider implements LoggingProvider {

    private static Semaphore semaphore = new Semaphore(-1);
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(5000) {
                @Override
                public Runnable take() throws InterruptedException {
                    semaphore.acquire();
                    return super.take();
                }
            }, r -> {
        Thread thread = new Thread(r, "Remora logger thread" + System.currentTimeMillis());
        thread.setDaemon(true);
        return thread;
    });
    private static ArrayList<RemoraLoggingProvider> instances = new ArrayList<>();
    private TinylogLoggingProvider realProvider;

    public RemoraLoggingProvider() {
        instances.add(this);
    }

    public static void startLogging() {
        instances.stream().forEach(a -> a.realProvider = new TinylogLoggingProvider());
        semaphore.release(Integer.MAX_VALUE);
        threadPoolExecutor.shutdown();
    }

    @Override
    public ContextProvider getContextProvider() {
        return realProvider.getContextProvider();
    }

    @Override
    public Level getMinimumLevel() {
        return Level.TRACE;
    }

    @Override
    public Level getMinimumLevel(String tag) {
        return Level.TRACE;
    }

    @Override
    public boolean isEnabled(int depth, String tag, Level level) {
        return realProvider.isEnabled(depth + 1, tag, level);
    }

    @Override
    public void log(int depth, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj,
                    Object... arguments) {
        BaseTransformers adviceByName = null;
        if (arguments != null && arguments[0] instanceof BaseTransformers) {
            adviceByName = ((BaseTransformers) arguments[0]);
        }
        if (arguments != null && arguments[0] instanceof BaseTransformers.InterceptionContext) {
            adviceByName = ((BaseTransformers.InterceptionContext) arguments[0]).interceptorInstance;
        }

        if (adviceByName != null) {
            Level advicesLogLevel = adviceByName.getLogLevel();
            if (advicesLogLevel.ordinal() > level.ordinal()) {
                return;
            }
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }
        if (semaphore.availablePermits() < 0) {
            // String className = RuntimeProvider.getCallerStackTraceElement(depth + 1).getClassName();
            Object[] finalArguments = arguments;
            threadPoolExecutor.submit(() -> {
                realProvider.log(depth + 1, tag, level, exception, formatter, obj, finalArguments);
            });
        } else {
            realProvider.log(depth + 1, tag, level, exception, formatter, obj, arguments);
        }

    }

    @Override
    public void log(String loggerClassName, String tag, Level level, Throwable exception, MessageFormatter formatter,
                    Object obj, Object... arguments) {
        realProvider.log(loggerClassName, tag, level, exception, formatter, obj, arguments);

    }

    @Override
    public void shutdown() throws InterruptedException {
        threadPoolExecutor.shutdownNow();
        realProvider.shutdown();

    }

}
