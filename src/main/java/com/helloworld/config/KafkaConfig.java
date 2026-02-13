package com.helloworld.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Active when app.features.kafka.enabled=true. Requires Kafka running (e.g. docker-compose up -d kafka).
 */
@Configuration
@ConditionalOnProperty(name = "app.features.kafka.enabled", havingValue = "true")
@Import(KafkaAutoConfiguration.class)
public class KafkaConfig {
}
