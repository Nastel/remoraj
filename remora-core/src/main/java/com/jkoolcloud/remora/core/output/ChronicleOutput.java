package com.jkoolcloud.remora.core.output;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;

public class ChronicleOutput implements OutputManager.AgentOutput<EntryDefinition> {

	Logger logger = Logger.getLogger(ChronicleOutput.class.getName());

	private ExcerptAppender appender;
	private ChronicleQueue queue;
	@RemoraConfig.Configurable
	String queuePath = "./tmp/probe";

	@Override
	public void init() {
		File queueDir = null;

		queueDir = Paths.get(queuePath).toFile();
		logger.info("Writing to " + queueDir.getAbsolutePath());

		ChronicleQueue queue = ChronicleQueue.single(queueDir.getPath());

		if (queue != null) {
			logger.info("Queue initialized " + this);
		} else {
			logger.severe("Queue failed");
		}

		appender = queue.acquireAppender();
		if (appender != null) {
			logger.info("Appender initialized");
		} else {
			logger.severe("Appender failed");
		}

	}

	@Override
	public void send(EntryDefinition entry) {
		appender.writeDocument(entry);
	}

	@Override
	public void shutdown() {
		logger.info("Shutting down chronicle queue:" + this);
		if (queue != null) {
			queue.close();
		}
	}

}
