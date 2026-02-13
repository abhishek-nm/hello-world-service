package com.helloworld.contract;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wraps all successful API responses under /api/v1 in the universal envelope.
 * Fills requestId, status=SUCCESS, and meta (queryTimeMs, fromCache, degraded, version).
 */
@RestControllerAdvice(basePackages = "com.helloworld.controller")
public class UniversalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Value("${app.api.version:v1}")
    private String apiVersion;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String path = servletRequest.getServletRequest().getRequestURI();
            if (!path.startsWith("/api/v1")) {
                return body;
            }
        }

        if (body instanceof UniversalResponse) {
            UniversalResponse<?> existing = (UniversalResponse<?>) body;
            if (existing.getMeta() == null) {
                existing.setMeta(buildMeta(false, false));
            }
            return existing;
        }

        String requestId = RequestContextHolder.getRequestId();
        ResponseMeta meta = buildMeta(false, false);
        return UniversalResponse.success(body, requestId, meta);
    }

    private ResponseMeta buildMeta(boolean fromCache, boolean degraded) {
        return new ResponseMeta(
                RequestContextHolder.queryTimeMs(),
                fromCache,
                degraded,
                apiVersion
        );
    }
}
