package com.sl.mecm.service.gateway.authorize;

import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.service.gateway.constants.Variables;
import com.sl.mecm.service.gateway.service.TokenService;
import com.sl.mecm.service.gateway.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@DependsOn("mecmTokenService")
@Slf4j
public class AccessTokenWebFilter implements WebFilter {

    @Autowired
    private TokenService mecmTokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authId = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(Variables.AUTH_ID))
                .orElseGet(() -> new HttpCookie(Variables.AUTH_ID, null)).getValue();
        String authToken = exchange.getRequest().getHeaders().getFirst(Variables.AUTH_TOKEN);
        log.info("AccessTokenWebFilter - authId:" + authId + "; authToken:" + authToken);
        String accessToken = TokenUtils.applyAccessToken(authId, authToken);
        log.info("AccessTokenWebFilter - accessToken:" + accessToken);
        String sessionToken = StringUtils.hasText(accessToken) ? mecmTokenService.getSessionToken(accessToken) : null;
        ServerHttpRequest newRequest = exchange.getRequest().mutate().header(CommonVariables.SESSION_TOKEN, sessionToken).build();
        return chain.filter(exchange.mutate().request(newRequest).build());
    }
}
