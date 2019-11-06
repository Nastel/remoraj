package com.jkoolcloud.remora.core;

import java.util.Stack;

import org.tinylog.TaggedLogger;

public class CallStack<T> extends Stack<EntryDefinition> {
	private final TaggedLogger logger;

	private String application;

	public CallStack(TaggedLogger logger) {
		this.logger = logger;
	}

	@Override
	public EntryDefinition push(EntryDefinition item) {
		logger.info("Stack push: " + (size() + 1));
		item.setApplication(application);
		return super.push(item);
	}

	@Override
	public synchronized EntryDefinition pop() {
		logger.info("Stack pop: " + (size() - 1));
		return super.pop();
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
		for (int i = 0; i <= size(); i++) {
			get(i).setApplication(application);
		}
	}
}
