package com.helloworld.contract;

/**
 * Authoritative signal for API response state. Clients must rely on this, not HTTP status alone.
 */
public enum ResponseStatus {
    /** All data available. */
    SUCCESS,
    /** Some entities or operations failed; partial data may be present. */
    PARTIAL,
    /** Async work in progress; data may be incomplete. */
    LOADING,
    /** Request processed but no usable data (e.g. validation failed, dependency error). */
    FAILED
}
