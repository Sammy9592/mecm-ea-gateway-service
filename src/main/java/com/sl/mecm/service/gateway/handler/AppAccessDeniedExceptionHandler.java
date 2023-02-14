package com.sl.mecm.service.gateway.handler;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.service.gateway.exceptions.ErrorResponseResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AppAccessDeniedExceptionHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return ErrorResponseResolver.authErrorResponse(exchange, "Access Denied!");
    }
}
