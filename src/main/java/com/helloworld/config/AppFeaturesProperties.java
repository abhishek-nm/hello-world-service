package com.helloworld.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Feature flags for organisation boilerplate.
 * Enable only when the corresponding infrastructure is available; fallbacks are used otherwise.
 */
@Component
@ConfigurationProperties(prefix = "app.features")
public class AppFeaturesProperties {

    private boolean postgres = false;
    private boolean redis = false;
    private boolean elasticsearch = false;
    private boolean monitoring = true;
    private final Apm apm = new Apm();
    private final Kafka kafka = new Kafka();
    private final Rabbitmq rabbitmq = new Rabbitmq();
    private final Statemachine statemachine = new Statemachine();

    public static class Kafka {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Rabbitmq {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public boolean isPostgres() {
        return postgres;
    }

    public void setPostgres(boolean postgres) {
        this.postgres = postgres;
    }

    public boolean isRedis() {
        return redis;
    }

    public void setRedis(boolean redis) {
        this.redis = redis;
    }

    public boolean isElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(boolean elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public static class Apm {
        private boolean enabled = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public boolean isApm() {
        return apm.isEnabled();
    }

    public Apm getApm() {
        return apm;
    }

    public boolean isKafka() {
        return kafka.isEnabled();
    }

    public Kafka getKafka() {
        return kafka;
    }

    public boolean isRabbitmq() {
        return rabbitmq.isEnabled();
    }

    public Rabbitmq getRabbitmq() {
        return rabbitmq;
    }

    public static class Statemachine {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public boolean isStatemachine() {
        return statemachine.isEnabled();
    }

    public Statemachine getStatemachine() {
        return statemachine;
    }
}
