package com.jkoolcloud.remora.core.output;

import java.io.File;
import java.nio.file.Paths;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;

public class ChronicleOutput implements OutputManager.AgentOutput<EntryDefinition> {

	TaggedLogger logger = Logger.tag("INIT");

	private ExcerptAppender appender;
	private ChronicleQueue queue;
	@RemoraConfig.Configurable
	String queuePath = System.getProperty(Remora.REMORA_PATH) + "/queue";

	@Override
	public void init() {
		File queueDir = Paths.get(queuePath).toFile();

		logger.info("Writing to " + queueDir.getAbsolutePath());

		queue = ChronicleQueue.single(queueDir.getPath());

		if (queue != null) {
			logger.info("Queue initialized " + this);
		} else {
			logger.error("Queue failed");
		}

		appender = queue.acquireAppender();
		if (appender != null) {
			logger.info("Appender initialized");
		} else {
			logger.error("Appender failed");
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
