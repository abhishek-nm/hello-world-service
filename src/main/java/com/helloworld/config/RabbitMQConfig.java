package com.helloworld.config;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Active when app.features.rabbitmq.enabled=true. Requires RabbitMQ running (e.g. docker-compose up -d rabbitmq).
 */
@Configuration
@ConditionalOnProperty(name = "app.features.rabbitmq.enabled", havingValue = "true")
@Import(RabbitAutoConfiguration.class)
public class RabbitMQConfig {

    /** Default queue name. Override via app.rabbitmq.queue-name if needed. */
    public static final String DEFAULT_QUEUE = "sample.queue";

    @Bean
    public Queue sampleQueue() {
        return new Queue(DEFAULT_QUEUE, true);
    }
}
