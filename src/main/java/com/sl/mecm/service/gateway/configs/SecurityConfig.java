package com.sl.mecm.service.gateway.configs;

import com.sl.mecm.core.commons.web.UserAccountInfo;
import com.sl.mecm.service.gateway.authorize.AccessTokenWebFilter;
import com.sl.mecm.service.gateway.handler.AppAccessDeniedExceptionHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    @Autowired
    private AccessTokenWebFilter accessTokenWebFilter;

    @Autowired
    private ServerSecurityContextRepository appUserSecurityContextRepository;

//    @Bean
//    SecurityWebFilterChain authFilterChain(ServerHttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeExchange(spec -> spec
//                        .pathMatchers(HttpMethod.POST, "/mecm/signup", "/mecm/login").permitAll()
//                        .anyExchange().access(new ReactiveAuthorizationManager<AuthorizationContext>() {
//                            @Override
//                            public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
//                                return null;
//                            }
//                        }))
//                .securityContextRepository(appUserSecurityContextRepository)
//                .exceptionHandling(spec -> spec.accessDeniedHandler(new AppAccessDeniedExceptionHandler()));
//        return http.build();
//    }

    @Bean
    SecurityWebFilterChain serviceFilterChain(ServerHttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeExchange(spec -> spec
                        .pathMatchers(HttpMethod.POST, "/mecm/signup", "/mecm/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/mecm/user/**").hasRole("MER_ADMIN")
                        .anyExchange().denyAll())
                .addFilterBefore(accessTokenWebFilter, SecurityWebFiltersOrder.REACTOR_CONTEXT)
                .securityContextRepository(appUserSecurityContextRepository)
                .exceptionHandling(spec -> spec.accessDeniedHandler(new AppAccessDeniedExceptionHandler()));
        return http.build();
    }
}