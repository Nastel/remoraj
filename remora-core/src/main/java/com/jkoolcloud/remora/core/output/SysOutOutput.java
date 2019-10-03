package com.jkoolcloud.remora.core.output;

import com.jkoolcloud.remora.core.EntryDefinition;

public class SysOutOutput implements OutputManager.AgentOutput<EntryDefinition> {

	@Override
	public void init() {
	}

	@Override
	public void send(EntryDefinition entry) {
		System.out.println(entry);
	}

	@Override
	public void shutdown() {

	}
}
