/*
 *
 * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.jkoolcloud.remora.advices;

import java.io.BufferedInputStream;
import java.io.FileReader;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaEchoTestClient {

	public static final String CONSUMMER_PROPERTIES = "../../../../consumer.properties";
	public static final String PRODUCER_PROPERTIES = "../../../../producer.properties";
	private static String receiveTopicName;
	private static String sendTopicName;

	public static void main(String[] args) throws Exception {
		Consumer<String, String> consumer = initConsumer();
		Producer<String, String> producer = initProducer();

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
			Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
			while (iterator.hasNext()) {
				ConsumerRecord<String, String> next = iterator.next();
				System.out.println("Key " + next.key() + "Value " + next.value());
				producer.send(new ProducerRecord<>(sendTopicName, next.key(), "Echo" + next.value()));
			}
		}
	}

	private static Consumer<String, String> initConsumer() throws Exception {
		Properties props = new Properties();
		props.load(new FileReader(KafkaEchoTestClient.class.getResource(CONSUMMER_PROPERTIES).getFile())); // NON-NLS
		receiveTopicName = props.getProperty("test.app.topic.name", "tnt4j_streams_kafka_intercept_test_page_visits"); // NON-NLS
		props.remove("test.app.topic.name");

		Consumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(receiveTopicName));

		return consumer;
	}

	private static Producer<String, String> initProducer() throws Exception {
		Properties props = new Properties();
		props.load(new BufferedInputStream(KafkaEchoTestClient.class.getResourceAsStream(PRODUCER_PROPERTIES)));// NON-NLS

		Integer eventsToProduce = Integer.valueOf(props.getProperty("events.count"), 10);
		props.remove("events.count");

		sendTopicName = props.getProperty("test.app.topic.name", "tnt4j_streams_kafka_intercept_test_page_visits"); // NON-NLS

		props.remove("test.app.topic.name");

		Producer<String, String> producer = new KafkaProducer<>(props);

		return producer;
	}
}
