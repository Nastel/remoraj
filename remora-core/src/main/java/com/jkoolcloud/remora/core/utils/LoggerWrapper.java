package com.jkoolcloud.remora.core.utils;

import java.text.MessageFormat;

public class LoggerWrapper {
	public static void pLog(String message) {
		System.out.println(message);
	}

	public static void pLog(String message, Object... arguments) {
		System.out.println(MessageFormat.format(message, arguments));
	}
}