package com.helloworld;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RabbitAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
public class HelloWorldServiceApplication {

    public static void main(String[] args) {
        // Propagate traces and metrics to Elastic APM when server URL is set (or ELASTIC_APM_ENABLED=true)
        String serverUrl = System.getenv("ELASTIC_APM_SERVER_URL");
        String enabled = System.getenv("ELASTIC_APM_ENABLED");
        if ((serverUrl != null && !serverUrl.isBlank()) || "true".equalsIgnoreCase(enabled)) {
            ElasticApmAttacher.attach();
            System.out.println("[APM] Agent attached; server=" + serverUrl);
        }
        SpringApplication.run(HelloWorldServiceApplication.class, args);
    }
}
