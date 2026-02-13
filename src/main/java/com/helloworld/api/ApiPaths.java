package com.helloworld.api;

/**
 * Central place for API path constants. Change base path or version here when defining your service API.
 */
public final class ApiPaths {

    /** Base path for all REST APIs. Version in URL per org standard; breaking change = new major version. */
    public static final String BASE = "/api/v1";

    /** Sample resource path (neutral name). Replace with your first resource (e.g. /api/orders). */
    public static final String SAMPLES = BASE + "/samples";

    /** Status/ops endpoints (health, features, etc.). */
    public static final String STATUS = BASE + "/status";

    /** Event/messaging test endpoints (send to RabbitMQ or Kafka when enabled). */
    public static final String EVENTS = BASE + "/events";

    /** State machine example (workflow demo when app.features.statemachine.enabled=true). */
    public static final String STATEMACHINE = BASE + "/statemachine";

    private ApiPaths() {
    }
}
