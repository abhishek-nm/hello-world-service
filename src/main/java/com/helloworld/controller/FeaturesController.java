package com.helloworld.controller;

import com.helloworld.api.ApiPaths;
import com.helloworld.config.AppFeaturesProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Ops endpoint: which feature flags (Postgres, Redis, ES, monitoring) are enabled.
 * Keep as-is for debugging and runbooks; add more status endpoints under {@link ApiPaths#STATUS} if needed.
 */
@RestController
@RequestMapping(ApiPaths.STATUS)
public class FeaturesController {

    private final AppFeaturesProperties features;

    public FeaturesController(AppFeaturesProperties features) {
        this.features = features;
    }

    @GetMapping("/features")
    public Map<String, Boolean> features() {
        return Map.of(
                "postgres", features.isPostgres(),
                "redis", features.isRedis(),
                "elasticsearch", features.isElasticsearch(),
                "monitoring", features.isMonitoring(),
                "apm", features.isApm(),
                "kafka", features.isKafka(),
                "rabbitmq", features.isRabbitmq(),
                "statemachine", features.isStatemachine()
        );
    }
}
