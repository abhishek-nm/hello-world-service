package com.helloworld.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Universal request body pattern: business payload in {@code data}, context in {@code meta}.
 * Use for POST/PUT when you want the org-standard envelope; GETs typically have no body.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversalRequest<T> {
    private T data;
    private RequestMeta meta;

    public UniversalRequest() {
    }

    public UniversalRequest(T data, RequestMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public RequestMeta getMeta() {
        return meta;
    }

    public void setMeta(RequestMeta meta) {
        this.meta = meta;
    }
}
