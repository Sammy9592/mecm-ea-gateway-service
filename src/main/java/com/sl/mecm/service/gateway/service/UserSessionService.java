package com.sl.mecm.service.gateway.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.http.HttpService;
import com.sl.mecm.core.commons.web.UserAccountInfo;
import com.sl.mecm.service.gateway.authorize.MECMUserAuthenticationToken;
import com.sl.mecm.service.gateway.configs.CacheServiceConfigs;
import com.sl.mecm.service.gateway.exceptions.AuthException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserSessionService extends HttpService {

    @Autowired
    private WebClient cacheWebClient;

    @Autowired
    private CacheServiceConfigs cacheServiceConfigs;

    public Mono<SecurityContext> retrieveSession(String sessionToken){
        if (!StringUtils.hasText(sessionToken)){
            return Mono.just(new SecurityContextImpl(unAuthToken()));
        }
        return reactivePost(cacheServiceConfigs.getPathQueryCache(), JSONObject.of(CommonVariables.DATA_KEY, sessionToken), new HashMap<>())
                .map(JSON::parseObject)
                .map(bodyObject -> {
                    if (!isResponseSuccess(bodyObject)){
                        throw new AuthException("503", "user session not found by:[" + sessionToken + "]", bodyObject);
                    }
                    return bodyObject.getJSONObject(CommonVariables.DATA);
                })
                .map(dataObject -> dataObject.to(UserAccountInfo.class))
                .map(userAccountInfo -> new MECMUserAuthenticationToken(userAccountInfo, true))
                .map((Function<MECMUserAuthenticationToken, SecurityContext>) SecurityContextImpl::new)
                .onErrorResume(AuthException.class, authException -> {
                    log.info("session expired, msg:" + JSON.toJSONString(authException));
                    return Mono.just(new SecurityContextImpl(unAuthToken()));
                })
                .onErrorResume(e -> {
                    log.error("can not retrieve session because of:" + e.getLocalizedMessage(), e);
                    return Mono.just(new SecurityContextImpl(unAuthToken()));
                });
    }

    @Override
    protected RestTemplate thisRestTemplate() {
        return null;
    }

    @Override
    protected WebClient thisWebClient() {
        return cacheWebClient;
    }

    private boolean isResponseSuccess(JSONObject bodyObject){
        String code = bodyObject.getString(CommonVariables.CODE);
        return "200".equals(code) && bodyObject.get(CommonVariables.DATA) != null;
    }

    private MECMUserAuthenticationToken unAuthToken(){
        return new MECMUserAuthenticationToken(UserAccountInfo.createEmptyUserAccount(), false);
    }
}
