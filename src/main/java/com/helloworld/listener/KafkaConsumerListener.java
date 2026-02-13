package com.helloworld.listener;

import com.helloworld.messaging.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Generic Kafka consumer for the default topic. Only active when app.features.kafka.enabled=true.
 * Replace or add more @KafkaListener methods for your topics; use this as the pattern.
 */
@Component
@ConditionalOnProperty(name = "app.features.kafka.enabled", havingValue = "true")
public class KafkaConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerListener.class);

    @KafkaListener(topics = KafkaProducerService.DEFAULT_TOPIC, groupId = "${spring.kafka.consumer.group-id:hello-world-service}")
    public void receive(String message) {
        log.info("[Kafka] Received from {}: {}", KafkaProducerService.DEFAULT_TOPIC, message);
    }
}
