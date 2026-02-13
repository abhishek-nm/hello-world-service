package com.helloworld.messaging;

import com.helloworld.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Sends messages to RabbitMQ. Only active when app.features.rabbitmq.enabled=true.
 * Use this service to publish events from your business logic.
 */
@Service
@ConditionalOnProperty(name = "app.features.rabbitmq.enabled", havingValue = "true")
public class RabbitProducerService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send a string message to the default sample queue.
     */
    public void send(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DEFAULT_QUEUE, message);
    }

    /**
     * Send to a specific queue.
     */
    public void send(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
