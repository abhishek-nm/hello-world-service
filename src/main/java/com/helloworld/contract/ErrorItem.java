package com.helloworld.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Structured error entry. errorCode is stable and documented; message is client-safe.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorItem {
    private String errorCode;
    private String message;
    private String source;   // e.g. "TBO", "PAYMENTS"
    private Boolean retryable;
    /** For validation: field path -> message (e.g. "checkIn" -> "must be in the future"). */
    private Map<String, String> fields;

    public ErrorItem() {
    }

    public ErrorItem(String errorCode, String message, String source, Boolean retryable, Map<String, String> fields) {
        this.errorCode = errorCode;
        this.message = message;
        this.source = source;
        this.retryable = retryable;
        this.fields = fields;
    }

    public static ErrorItem of(String errorCode, String message) {
        ErrorItem e = new ErrorItem();
        e.setErrorCode(errorCode);
        e.setMessage(message);
        return e;
    }

    public static ErrorItem validationError(Map<String, String> fields) {
        ErrorItem e = new ErrorItem();
        e.setErrorCode("INVALID_REQUEST");
        e.setMessage("Request validation failed");
        e.setFields(fields);
        return e;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
