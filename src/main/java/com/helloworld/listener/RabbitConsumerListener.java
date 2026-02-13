package com.helloworld.listener;

import com.helloworld.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes messages from the sample queue. Only active when app.features.rabbitmq.enabled=true.
 * Replace or extend with your domain logic (e.g. persist, call another service).
 */
@Component
@ConditionalOnProperty(name = "app.features.rabbitmq.enabled", havingValue = "true")
public class RabbitConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumerListener.class);

    @RabbitListener(queues = RabbitMQConfig.DEFAULT_QUEUE)
    public void receive(String message) {
        log.info("[RabbitMQ] Received from {}: {}", RabbitMQConfig.DEFAULT_QUEUE, message);
    }
}
