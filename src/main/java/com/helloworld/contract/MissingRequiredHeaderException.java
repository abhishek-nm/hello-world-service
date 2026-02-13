package com.helloworld.contract;

/**
 * Thrown when a mandatory API header is missing (X-Client-Id, X-Client-Version, or
 * X-Idempotency-Key for POST/PUT/PATCH). Handled by {@link UniversalContractExceptionHandler}
 * to return 400 with UniversalResponse envelope.
 */
public class MissingRequiredHeaderException extends RuntimeException {
    private final String headerName;

    public MissingRequiredHeaderException(String headerName) {
        super("Missing required header: " + headerName);
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
