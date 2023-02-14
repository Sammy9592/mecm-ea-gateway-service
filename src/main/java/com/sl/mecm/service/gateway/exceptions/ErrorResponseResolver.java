package com.sl.mecm.service.gateway.exceptions;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;

import reactor.core.publisher.Mono;

public class ErrorResponseResolver {

    private ErrorResponseResolver(){}

    public static Mono<Void> authErrorResponse(ServerWebExchange exchange, String message){
        return Mono.just(exchange)
                .map(ServerWebExchange::getResponse)
                .flatMap(response -> {
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBuffer buffer = response.bufferFactory().wrap(
                            JSONObject.of()
                                    .fluentPut(CommonVariables.CODE, ErrorCode.NO_AUTH.getCode())
                                    .fluentPut(CommonVariables.MESSAGE, message)
                                    .toString()
                                    .getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer).doOnError(e -> DataBufferUtils.release(buffer)));
                });
    }
}
