package com.helloworld.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Operational signals for every response. Mandatory: queryTimeMs, fromCache, degraded.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMeta {
    private Long queryTimeMs;
    private Boolean fromCache;
    private Boolean degraded;
    private String version;  // e.g. "v1"

    public ResponseMeta() {
    }

    public ResponseMeta(Long queryTimeMs, Boolean fromCache, Boolean degraded, String version) {
        this.queryTimeMs = queryTimeMs;
        this.fromCache = fromCache;
        this.degraded = degraded;
        this.version = version;
    }

    public Long getQueryTimeMs() {
        return queryTimeMs;
    }

    public void setQueryTimeMs(Long queryTimeMs) {
        this.queryTimeMs = queryTimeMs;
    }

    public Boolean getFromCache() {
        return fromCache;
    }

    public void setFromCache(Boolean fromCache) {
        this.fromCache = fromCache;
    }

    public Boolean getDegraded() {
        return degraded;
    }

    public void setDegraded(Boolean degraded) {
        this.degraded = degraded;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
