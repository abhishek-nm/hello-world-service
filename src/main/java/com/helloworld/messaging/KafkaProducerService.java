package com.helloworld.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Generic Kafka producer. Only active when app.features.kafka.enabled=true.
 * Use for publishing events to any topic; extend with typed methods as needed.
 */
@Service
@ConditionalOnProperty(name = "app.features.kafka.enabled", havingValue = "true")
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    /** Default topic for sample events. Override via config or pass topic per call. */
    public static final String DEFAULT_TOPIC = "sample-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send message to the default topic with no key.
     */
    public CompletableFuture<SendResult<String, String>> send(String value) {
        return send(DEFAULT_TOPIC, null, value);
    }

    /**
     * Send message to the default topic with optional key.
     */
    public CompletableFuture<SendResult<String, String>> send(String key, String value) {
        return send(DEFAULT_TOPIC, key, value);
    }

    /**
     * Send message to a specific topic. Key can be null for round-robin partition.
     */
    public CompletableFuture<SendResult<String, String>> send(String topic, String key, String value) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.warn("[Kafka] Send failed topic={} key={}: {}", topic, key, ex.getMessage());
            } else if (result != null) {
                log.debug("[Kafka] Sent topic={} partition={} offset={}", topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
        return future;
    }
}
