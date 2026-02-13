package com.helloworld.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maps exceptions to the universal response envelope. Validation errors return 400 with
 * status=FAILED and errors[].fields; missing headers return 400; other errors return 500/503.
 */
@RestControllerAdvice(basePackages = "com.helloworld.controller")
public class UniversalContractExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UniversalContractExceptionHandler.class);

    @Value("${app.api.version:v1}")
    private String apiVersion;

    private String requestId() {
        String id = RequestContextHolder.getRequestId();
        return id != null ? id : UUID.randomUUID().toString();
    }

    private boolean isConnectionFailure(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof ConnectException) {
                return true;
            }
            String msg = t.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("connection refused") || lower.contains("connection reset")
                        || lower.contains("nodename nor servname provided") || lower.contains("unknown host")
                        || lower.contains("rabbitmq") || lower.contains("amqp")) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    @ExceptionHandler(MissingRequiredHeaderException.class)
    public ResponseEntity<UniversalResponse<Void>> handleMissingHeader(MissingRequiredHeaderException ex) {
        String requestId = requestId();
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, false, apiVersion);
        ErrorItem error = ErrorItem.of("MISSING_HEADER", "Missing required header: " + ex.getHeaderName());
        UniversalResponse<Void> body = UniversalResponse.failed(List.of(error), requestId, meta);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UniversalResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String requestId = requestId();
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, false, apiVersion);
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"));
        ErrorItem error = ErrorItem.validationError(fields);
        UniversalResponse<Void> body = UniversalResponse.failed(List.of(error), requestId, meta);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<UniversalResponse<Void>> handleRabbitMQ(AmqpException ex) {
        log.warn("RabbitMQ error: {}", ex.getMessage());
        String requestId = requestId();
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, true, apiVersion);
        ErrorItem error = ErrorItem.of("RABBITMQ_UNAVAILABLE", "RabbitMQ is temporarily unavailable. Check that RabbitMQ is running and reachable.");
        error.setRetryable(true);
        UniversalResponse<Void> body = UniversalResponse.failed(List.of(error), requestId, meta);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<UniversalResponse<Void>> handleKafka(KafkaException ex) {
        log.warn("Kafka error: {}", ex.getMessage());
        String requestId = requestId();
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, true, apiVersion);
        ErrorItem error = ErrorItem.of("KAFKA_UNAVAILABLE", "Kafka is temporarily unavailable. Check that Kafka is running and reachable.");
        error.setRetryable(true);
        UniversalResponse<Void> body = UniversalResponse.failed(List.of(error), requestId, meta);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<UniversalResponse<Void>> handleOther(Throwable ex) {
        log.error("Unexpected error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // Handle wrapped RabbitMQ/Kafka exceptions (e.g. from Spring's NestedRuntimeException)
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof AmqpException) {
                return handleRabbitMQ((AmqpException) cause);
            }
            if (cause instanceof KafkaException) {
                return handleKafka((KafkaException) cause);
            }
            cause = cause.getCause();
        }

        // Connection refused / unreachable often means RabbitMQ or Kafka is down (even if not AmqpException)
        if (isConnectionFailure(ex)) {
            log.warn("Connection failure (likely RabbitMQ/Kafka unreachable): {}", ex.getMessage());
            String requestId = requestId();
            ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, true, apiVersion);
            ErrorItem error = ErrorItem.of("SERVICE_UNAVAILABLE",
                    "Broker unreachable. Ensure RabbitMQ/Kafka is running and the app can connect (e.g. docker compose up -d).");
            error.setRetryable(true);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(UniversalResponse.failed(List.of(error), requestId, meta));
        }

        String requestId = requestId();
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, true, apiVersion);
        ErrorItem error = ErrorItem.of("INTERNAL_ERROR", "An unexpected error occurred");
        error.setRetryable(false);
        UniversalResponse<Void> body = UniversalResponse.failed(Collections.singletonList(error), requestId, meta);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
