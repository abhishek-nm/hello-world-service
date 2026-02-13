package com.helloworld.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * Universal response envelope (org standard). Every API response must use this shape.
 * Clients must rely on {@link #status}, not HTTP code alone, for UI behavior.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversalResponse<T> {
    private String requestId;
    private ResponseStatus status;
    private T data;
    private List<ErrorItem> errors;
    private ResponseMeta meta;

    public UniversalResponse() {
    }

    public UniversalResponse(String requestId, ResponseStatus status, T data, List<ErrorItem> errors, ResponseMeta meta) {
        this.requestId = requestId;
        this.status = status;
        this.data = data != null ? data : (T) Collections.emptyMap();
        this.errors = errors != null ? errors : Collections.emptyList();
        this.meta = meta;
    }

    public static <T> UniversalResponse<T> success(T data, String requestId, ResponseMeta meta) {
        return new UniversalResponse<>(requestId, ResponseStatus.SUCCESS,
                data != null ? data : (T) Collections.emptyMap(),
                Collections.emptyList(), meta);
    }

    public static <T> UniversalResponse<T> partial(T data, List<ErrorItem> errors, String requestId, ResponseMeta meta) {
        return new UniversalResponse<>(requestId, ResponseStatus.PARTIAL,
                data != null ? data : (T) Collections.emptyMap(),
                errors != null ? errors : Collections.emptyList(), meta);
    }

    public static <T> UniversalResponse<T> loading(T data, String requestId, ResponseMeta meta) {
        return new UniversalResponse<>(requestId, ResponseStatus.LOADING,
                data != null ? data : (T) Collections.emptyMap(),
                Collections.emptyList(), meta);
    }

    public static <T> UniversalResponse<T> failed(List<ErrorItem> errors, String requestId, ResponseMeta meta) {
        return new UniversalResponse<>(requestId, ResponseStatus.FAILED,
                (T) Collections.emptyMap(),
                errors != null ? errors : Collections.emptyList(), meta);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorItem> errors) {
        this.errors = errors;
    }

    public ResponseMeta getMeta() {
        return meta;
    }

    public void setMeta(ResponseMeta meta) {
        this.meta = meta;
    }
}
