package com.helloworld.contract;

/**
 * Holds request-scoped values (requestId, startTime) for the current thread.
 * Set in {@link UniversalContractFilter}, read by response advice and exception handler.
 */
public final class RequestContextHolder {
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_TIME_MS = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setStartTimeMs(long startTimeMs) {
        START_TIME_MS.set(startTimeMs);
    }

    public static Long getStartTimeMs() {
        return START_TIME_MS.get();
    }

    public static long queryTimeMs() {
        Long start = START_TIME_MS.get();
        return start != null ? Math.max(0, System.currentTimeMillis() - start) : 0L;
    }

    public static void clear() {
        REQUEST_ID.remove();
        START_TIME_MS.remove();
    }
}
