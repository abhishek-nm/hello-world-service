package com.helloworld.controller;

import com.helloworld.api.ApiPaths;
import com.helloworld.dto.EventMessageRequest;
import com.helloworld.messaging.KafkaProducerService;
import com.helloworld.messaging.RabbitProducerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test endpoints to send a message to RabbitMQ or Kafka when the corresponding feature is enabled.
 * For production, call RabbitProducerService / KafkaProducerService from your business logic instead.
 * Returns Map (not String) so the universal response advice can wrap the body without triggering StringHttpMessageConverter.
 */
@RestController
@RequestMapping(ApiPaths.EVENTS)
public class EventsController {

    @Autowired(required = false)
    private RabbitProducerService rabbitProducer;

    @Autowired(required = false)
    private KafkaProducerService kafkaProducer;

    @GetMapping
    public ResponseEntity<Map<String, String>> eventsInfo() {
        return ResponseEntity.ok(Map.of(
                "message", "POST /api/v1/events/rabbit or POST /api/v1/events/kafka with body {\"message\": \"...\"}",
                "rabbitmq", rabbitProducer != null ? "enabled" : "disabled",
                "kafka", kafkaProducer != null ? "enabled" : "disabled"
        ));
    }

    @PostMapping("/rabbit")
    public ResponseEntity<Map<String, String>> sendToRabbit(@Valid @RequestBody EventMessageRequest request) {
        if (rabbitProducer == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "RabbitMQ not enabled. Set app.features.rabbitmq.enabled=true and run RabbitMQ (e.g. docker-compose up -d rabbitmq)."));
        }
        rabbitProducer.send(request.getMessage());
        return ResponseEntity.accepted().body(Map.of("message", "Has been sent to RabbitMQ: with new message" + request.getMessage()));
    }

    @PostMapping("/kafka")
    public ResponseEntity<Map<String, String>> sendToKafka(@Valid @RequestBody EventMessageRequest request) {
        if (kafkaProducer == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Kafka not enabled. Set app.features.kafka.enabled=true and run Kafka (e.g. docker-compose up -d kafka)."));
        }
        kafkaProducer.send(request.getMessage());
        return ResponseEntity.accepted().body(Map.of("message", "Sent to Kafka: " + request.getMessage()));
    }
}
