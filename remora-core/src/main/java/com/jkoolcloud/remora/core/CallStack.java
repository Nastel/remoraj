package com.jkoolcloud.remora.core;

import java.util.Stack;

import org.tinylog.TaggedLogger;

public class CallStack<T> extends Stack<EntryDefinition> {
	private final TaggedLogger logger;

	public CallStack(TaggedLogger logger) {
		this.logger = logger;
	}

	@Override
	public EntryDefinition push(EntryDefinition item) {
		logger.info("Stack push: " + (size() + 1));
		return super.push(item);
	}

	@Override
	public synchronized EntryDefinition pop() {
		logger.info("Stack pop: " + (size() - 1));
		return super.pop();
	}
}
