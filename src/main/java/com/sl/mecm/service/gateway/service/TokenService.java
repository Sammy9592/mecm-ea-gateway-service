package com.sl.mecm.service.gateway.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMHttpException;
import com.sl.mecm.core.commons.exception.MECMServiceException;
import com.sl.mecm.core.commons.http.HttpService;
import com.sl.mecm.service.gateway.configs.TokenServiceConfig;
import com.sl.mecm.service.gateway.constants.Values;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service("mecmTokenService")
@Slf4j
public class TokenService extends HttpService {

    @Autowired
    private TokenServiceConfig tokenServiceConfig;

    @Autowired
    private RestTemplate tokenServiceRestTemplate;

    @Autowired
    private WebClient tokenWebClient;

    public String getSessionToken(String accessToken){
        Assert.hasText(accessToken, "access token must be not empty");
        try {
            JSONObject requestBody = JSONObject.of(CommonVariables.DATA_KEY, accessToken);
            String responseBody = post(tokenServiceConfig.getPathQuery(), requestBody, new HashMap<>());
            JSONObject responseObject = JSON.parseObject(responseBody);
            String code = responseObject.getString(CommonVariables.CODE);
            if (ErrorCode.SUCCESS.getCode().equals(code) && responseObject.getString(CommonVariables.DATA) != null){
                return responseObject.getString(CommonVariables.DATA);
            }else {
                log.error("session token not found with response:" + responseBody + ", by access token:" + accessToken);
            }
        }catch (MECMHttpException e){
            log.error("error on query session token, response status:" + e.getCode() + "; response:" + e.getResponseBody(), e);
        }catch (Exception e){
            log.error("error on query session token", e);
        }
        return null;
    }

    public Mono<String> getSessionTrustToken(String source, String sessionToken){
        Assert.hasText(source, "source must be not empty");
        Assert.hasText(sessionToken, "session token must be not empty");
        JSONObject requestBody =
                JSONObject.of()
                        .fluentPut(CommonVariables.AUTH_TYPE, Values.AUTH_TYPE_SESSION)
                        .fluentPut(
                                CommonVariables.AUTH_CERTS,
                                JSONObject.of()
                                        .fluentPut(CommonVariables.SOURCE, source)
                                        .fluentPut(CommonVariables.SESSION_TOKEN, sessionToken)
                        );
        return reactivePost(tokenServiceConfig.getPathSessionTrust(), requestBody, new HashMap<>())
                .onErrorResume(MECMHttpException.class, e -> {
                    log.error("error on retrieve session trust token, response status:" + e.getCode() + "; response:" + e.getResponseBody());
                    throw e;
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("error on retrieve session trust token:" + e.getLocalizedMessage());
                    throw new MECMServiceException(ErrorCode.ERROR.getCode(), e.getMessage(), null);
                });
    }

    public Mono<String> getClientTrustToken(String source, String secret){
        Assert.hasText(source, "source must be not empty");
        Assert.hasText(secret, "secret must be not empty");
        JSONObject requestBody =
                JSONObject.of()
                        .fluentPut(CommonVariables.AUTH_TYPE, Values.AUTH_TYPE_CLIENT)
                        .fluentPut(
                                CommonVariables.AUTH_CERTS,
                                JSONObject.of()
                                        .fluentPut(CommonVariables.SOURCE, source)
                                        .fluentPut(CommonVariables.SECRET, secret)
                        );
        return reactivePost(tokenServiceConfig.getPathClientTrust(), requestBody, new HashMap<>())
                .onErrorResume(MECMHttpException.class, e -> {
                    log.error("error on retrieve client trust token, response status:" + e.getCode() + "; response:" + e.getResponseBody());
                    throw e;
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("error on retrieve client trust token:" + e.getLocalizedMessage());
                    throw new MECMServiceException(ErrorCode.ERROR.getCode(), e.getMessage(), null);
                });
    }

    public void saveSessionToken(String accessToken, String sessionToken){
        Assert.hasText(accessToken, "access token must be not empty");
        Assert.hasText(sessionToken, "session token must be not empty");
        try {
            JSONObject requestBody = JSONObject.of(
                    CommonVariables.DATA_KEY, accessToken,
                    CommonVariables.DATA, sessionToken);
            log.info("save new session id:" + requestBody);
            String responseBody = post(tokenServiceConfig.getPathSave(), requestBody, new HashMap<>());
            log.info("save session token success, response:" + responseBody);
        }catch (MECMHttpException e){
            log.error("error on save session token, response status:" + e.getCode() + "; response:" + e.getResponseBody(), e);
            throw e;
        }
    }

    @Override
    protected RestTemplate thisRestTemplate() {
        return tokenServiceRestTemplate;
    }

    @Override
    protected WebClient thisWebClient() {
        return tokenWebClient;
    }
}
