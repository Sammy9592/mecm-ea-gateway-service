package com.sl.mecm.service.gateway.authorize.gateway;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.entity.AppResponse;
import com.sl.mecm.service.gateway.constants.Variables;
import com.sl.mecm.service.gateway.service.TokenService;
import com.sl.mecm.service.gateway.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory.NameConfig;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoginHandlerGatewayFilterFactory extends AbstractGatewayFilterFactory<NameConfig> {

    public LoginHandlerGatewayFilterFactory() {
        super(NameConfig.class);
    }

    @Value("${mecm.gateway-service.cookies-config.timeout}")
    private String cookieTimeoutStr;

    @Autowired
    private TokenService tokenService;

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("name");
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        final String sessionToken = TokenUtils.createSessionToken();
        return (exchange, chain) -> {
            if (config.getName().equals(exchange.getRequest().getPath().pathWithinApplication().value())) {
                ServerWebExchange newExchange = beforeRouteRequest(exchange, sessionToken);
                return chain
                        .filter(newExchange)
                        .then(Mono.fromRunnable(() -> afterRouteResponse(newExchange, sessionToken)));
            }
            return chain.filter(exchange);
        };
    }

    private ServerWebExchange beforeRouteRequest(ServerWebExchange originalExchange, String sessionToken){
        ServerHttpRequest request = originalExchange.getRequest();
        ServerHttpRequest newRequest = request.mutate().header(CommonVariables.SESSION_TOKEN, sessionToken).build();
        return originalExchange.mutate().request(newRequest).build();
    }

    private void afterRouteResponse(ServerWebExchange exchange, String sessionToken) {
        String authId = TokenUtils.createAuthId();
        String authToken = TokenUtils.createAuthToken();
        ServerHttpResponse response = exchange.getResponse();
        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode != null && statusCode.is2xxSuccessful()) {
            ResponseCookie cookie = ResponseCookie.from(Variables.AUTH_ID)
                    .httpOnly(true)
                    .maxAge(Integer.parseInt(cookieTimeoutStr))
                    .value(authId)
                    .build();
            response.getCookies().add(Variables.AUTH_ID, cookie);
            response.getHeaders().add(Variables.AUTH_TOKEN, authToken);
            log.info("new authId:" + authId);
            log.info("new authToken:" + authToken);
            log.info("new Session Token:" + sessionToken);
            String accessToken = TokenUtils.applyAccessToken(authId, authToken);
            try {
                tokenService.saveSessionToken(accessToken, sessionToken);
            }catch (Exception e){
                log.error("error on after login route:" + e.getMessage(), e);
                String errorResponseBody = new AppResponse("503", "login error!", null)
                        .toJSONObject().toString();
                DataBuffer dataBuffer = response.bufferFactory().wrap(errorResponseBody.getBytes(StandardCharsets.UTF_8));
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.writeWith(Mono.just(dataBuffer));
            }
        }
    }
}
