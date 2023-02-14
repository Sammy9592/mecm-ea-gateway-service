package com.sl.mecm.service.gateway.authorize;

import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.service.gateway.constants.Variables;
import com.sl.mecm.service.gateway.service.UserSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AppUserSecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    private UserSessionService userSessionService;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return null;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String sessionToken = exchange.getRequest().getHeaders().getFirst(CommonVariables.SESSION_TOKEN);
        log.info("load user session:" + sessionToken);
        return userSessionService.retrieveSession(sessionToken);
    }
}
