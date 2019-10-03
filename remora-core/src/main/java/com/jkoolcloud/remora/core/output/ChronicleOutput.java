package com.jkoolcloud.remora.core.output;

import java.io.File;
import java.nio.file.Paths;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;

public class ChronicleOutput implements OutputManager.AgentOutput<EntryDefinition> {

	private ExcerptAppender appender;
	private ChronicleQueue queue;
	@RemoraConfig.Configurable
	String queuePath = "c:\\tmp\\probe";

	@Override
	public void init() {
		File queueDir = null;

		queueDir = Paths.get(queuePath).toFile();
		System.out.println("Writing to " + queueDir.getAbsolutePath());

		ChronicleQueue queue = ChronicleQueue.single(queueDir.getPath());

		if (queue != null) {
			System.out.println("Queue initialized " + this);
		} else {
			System.out.println("Queue failed");
		}

		appender = queue.acquireAppender();
		if (appender != null) {
			System.out.println("Appender initialized");
		} else {
			System.out.println("Appender failed");
		}

	}

	@Override
	public void send(EntryDefinition entry) {
		appender.writeDocument(entry);
	}

	@Override
	public void shutdown() {
		System.out.println(this);
		if (queue != null) {
			queue.close();
		}
	}

	public ExcerptAppender getAppender() {
		return appender;
	}
}
