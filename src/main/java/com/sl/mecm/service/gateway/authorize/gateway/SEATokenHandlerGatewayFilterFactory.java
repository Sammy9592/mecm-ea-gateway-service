package com.sl.mecm.service.gateway.authorize.gateway;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;
import com.sl.mecm.core.commons.utils.JsonUtils;
import com.sl.mecm.service.gateway.constants.Variables;
import com.sl.mecm.service.gateway.exceptions.ErrorResponseResolver;
import com.sl.mecm.service.gateway.service.TokenService;
import com.sl.mecm.service.gateway.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory.NameConfig;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@DependsOn("mecmTokenService")
@Slf4j
public class SEATokenHandlingGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthCerts> {

    @Autowired
    private TokenService mecmTokenService;

    public SEATokenHandlingGatewayFilterFactory() {
        super(AuthCerts.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("source");
    }

    @Override
    public GatewayFilter apply(AuthCerts entitlement) {
        return (exchange, chain) -> Mono.just(exchange)
                .map(ServerWebExchange::getRequest)
                .flatMap(theRequest -> {
                    String sessionToken = theRequest.getHeaders().getFirst(CommonVariables.SESSION_TOKEN);
                    if (!StringUtils.hasText(sessionToken)){
                        throw new MECMServiceException(ErrorCode.NO_AUTH.getCode(), "Missing Auth Credential, session token is null", null);
                    }
                    return mecmTokenService.getSessionTrustToken(entitlement.getSource(), sessionToken)
                            .map(JsonUtils::toJsonObject)
                            .doOnNext(theResponseObject -> {
                                String code = theResponseObject.getString(CommonVariables.CODE);
                                if (!ErrorCode.SUCCESS.getCode().equals(code) || !StringUtils.hasText(theResponseObject.getString(CommonVariables.DATA))){
                                    log.error("error to retrieve trust token:" + theResponseObject);
                                    throw new MECMServiceException(ErrorCode.NO_AUTH.getCode(),
                                            "failed to retrieve trust token:" + theResponseObject.getString(CommonVariables.MESSAGE),
                                            theResponseObject);
                                }
                            })
                            .map(theResponseObject -> theResponseObject.getString(CommonVariables.DATA))
                            .map(theTrustToken -> theRequest.mutate()
                                    .header(CommonVariables.SESSION_TOKEN, sessionToken)
                                    .header(CommonVariables.MECM_TRUST_TOKEN, theTrustToken)
                                    .build())
                            .flatMap((Function<ServerHttpRequest, Mono<Void>>) newRequest ->
                                    chain.filter(exchange.mutate().request(newRequest).build()));
                })
                .onErrorResume(e -> {
                    log.warn("failed to check out auth token:" + e.getLocalizedMessage());
                    return ErrorResponseResolver.authErrorResponse(exchange, "Access Denied!");
                });
    }
}
