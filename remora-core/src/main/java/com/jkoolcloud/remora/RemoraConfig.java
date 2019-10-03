package com.jkoolcloud.remora;

import com.jkoolcloud.remora.core.output.OutputManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.jkoolcloud.remora.Remora.REMORA_PATH;
import static com.jkoolcloud.remora.core.utils.LoggerWrapper.pLog;

public enum RemoraConfig {
    INSTANCE;

    public static final String REMORA_PROPERTIES_FILE = "/config/remora.properties";
    Properties config;

    RemoraConfig() {
        init();
    }

    public static void configure(Object object) {

        Class<?> aClass = object.getClass();

        while (!aClass.equals(Object.class)) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Configurable.class)) {
                    field.setAccessible(true);

                    try {
                        switch (field.getType().getName()) {
                            case "java.lang.String":
                                field.set(object, getConfigValue(object.getClass(), field.getName()));
                                break;
                            case "java.util.List":
                                field.set(object, getList(getConfigValue(object.getClass(), field.getName())));
                                break;
                            case "default":
                                pLog("Unsuppoerted property");

                        }


                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
           aClass = aClass.getSuperclass();
        }
    }

    private static List getList(String configValue) {
        if (configValue == null)
            return null;
        String[] split = configValue.split(";");
        return  Arrays.asList(split).stream().map(v -> v.trim()).collect(Collectors.toList());
    }

    private static String getConfigValue(Class<?> aClass, String name) {
        Class workingClass = aClass;
        String value = null;
        while(value == null &&
                !workingClass.equals(Object.class)) {
            value = RemoraConfig.INSTANCE.config.getProperty(workingClass.getName() + "." + name);
            workingClass = aClass.getSuperclass();
        }
        return value;
    }

    protected void init() {
        config = new Properties();

            String remoraPath = System.getProperty(REMORA_PATH);
            File file = new File(remoraPath + REMORA_PROPERTIES_FILE);
        try ( FileInputStream inStream = new FileInputStream(file);) {
            config.load(inStream);
            config.size();
            pLog("Sucessfully loaded {0} properties from configuration file", config.size());
        } catch (IOException e) {
            pLog("Failed loading properties file");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Configurable {
    }

}
