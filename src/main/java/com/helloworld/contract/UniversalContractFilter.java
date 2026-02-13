package com.helloworld.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Enforces org-standard mandatory headers for /api/v1 and sets request context.
 * When strict mode is on: X-Request-Id (generated if absent), X-Client-Id, X-Client-Version,
 * and X-Idempotency-Key (for POST/PUT/PATCH) are required.
 */
@Component
@Order(1)
public class UniversalContractFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HEADER_CLIENT_VERSION = "X-Client-Version";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    @Value("${app.api.contract.strict-headers:true}")
    private boolean strictHeaders;

    @Value("${app.api.version:v1}")
    private String apiVersion;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1")) {
            return true;
        }
        if (!strictHeaders) {
            return false; // still run to set request id and start time
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestId = request.getHeader(HEADER_REQUEST_ID);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
                request.setAttribute(HEADER_REQUEST_ID, requestId);
            }
            response.setHeader(HEADER_REQUEST_ID, requestId);

            RequestContextHolder.setRequestId(requestId);
            RequestContextHolder.setStartTimeMs(System.currentTimeMillis());

            if (strictHeaders) {
                String clientId = request.getHeader(HEADER_CLIENT_ID);
                if (clientId == null || clientId.isBlank()) {
                    sendBadRequest(response, requestId, HEADER_CLIENT_ID);
                    return;
                }
                String clientVersion = request.getHeader(HEADER_CLIENT_VERSION);
                if (clientVersion == null || clientVersion.isBlank()) {
                    sendBadRequest(response, requestId, HEADER_CLIENT_VERSION);
                    return;
                }
                String method = request.getMethod();
                if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                    String idempotencyKey = request.getHeader(HEADER_IDEMPOTENCY_KEY);
                    if (idempotencyKey == null || idempotencyKey.isBlank()) {
                        sendBadRequest(response, requestId, HEADER_IDEMPOTENCY_KEY);
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }

    private void sendBadRequest(HttpServletResponse response, String requestId, String missingHeader) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseMeta meta = new ResponseMeta(RequestContextHolder.queryTimeMs(), false, false, apiVersion);
        UniversalResponse<?> body = UniversalResponse.failed(
                List.of(ErrorItem.of("MISSING_HEADER", "Missing required header: " + missingHeader)),
                requestId, meta);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
