package com.sl.mecm.service.gateway.authorize.gateway;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;
import com.sl.mecm.core.commons.utils.JsonUtils;
import com.sl.mecm.service.gateway.exceptions.ErrorResponseResolver;
import com.sl.mecm.service.gateway.service.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@DependsOn("mecmTokenService")
@Slf4j
public class CLATokenHandlerGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthCerts> {

    @Autowired
    private TokenService mecmTokenService;

    public CLATokenHandlerGatewayFilterFactory() {
        super(AuthCerts.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("source", "secret");
    }

    @Override
    public GatewayFilter apply(AuthCerts config) {
        if (!StringUtils.hasText(config.getSource()) || !StringUtils.hasText(config.getSecret())){
            throw new MECMServiceException(ErrorCode.SUCCESS.getCode(), "no auth certs be setup at gateway filter", null);
        }
        return (exchange, chain) -> Mono.just(config)
                .flatMap(authCerts -> mecmTokenService.getClientTrustToken(authCerts.getSource(), authCerts.getSecret())
                        .map(JsonUtils::toJsonObject)
                        .doOnNext(theResponseObject -> {
                            String code = theResponseObject.getString(CommonVariables.CODE);
                            if (!ErrorCode.SUCCESS.getCode().equals(code) || !StringUtils.hasText(theResponseObject.getString(CommonVariables.DATA))){
                                log.error("error to retrieve client trust token:" + theResponseObject);
                                throw new MECMServiceException(ErrorCode.NO_AUTH.getCode(),
                                        "failed to retrieve client trust token:" + theResponseObject.getString(CommonVariables.MESSAGE),
                                        theResponseObject);
                            }
                        })
                        .map(theResponseObject -> theResponseObject.getString(CommonVariables.DATA))
                        .map(theTrustToken -> exchange.getRequest().mutate()
                                .header(CommonVariables.MECM_TRUST_TOKEN, theTrustToken)
                                .build())
                        .flatMap(newRequest -> chain.filter(exchange.mutate().request(newRequest).build())))
                .onErrorResume(e -> {
                    log.warn("failed to check out client trust token:" + e.getLocalizedMessage());
                    return ErrorResponseResolver.authErrorResponse(exchange, "Access Denied!");
                });
    }
}
