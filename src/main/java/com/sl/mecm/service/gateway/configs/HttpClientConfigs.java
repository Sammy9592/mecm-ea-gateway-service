package com.sl.mecm.service.gateway.configs;


import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class HttpClientConfigs {

    @Autowired
    private TokenServiceConfig tokenServiceConfig;

    @Autowired
    private CacheServiceConfigs cacheServiceConfigs;

    @Bean("cacheWebClient")
    public WebClient createCacheServiceWebClient(){
        return createClient(cacheServiceConfigs.getTimeout());
    }

    @Bean("tokenWebClient")
    public WebClient createTokenServiceWebClient(){
        return createClient(tokenServiceConfig.getTimeout());
    }

    @Bean("tokenServiceRestTemplate")
    public RestTemplate createTokenServiceRestTemplate(){
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(tokenServiceConfig.getTimeout()))
                .setConnectionRequestTimeout(Timeout.ofSeconds(tokenServiceConfig.getTimeout()))
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .build();
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
        return new RestTemplate(factory);
    }

    private WebClient createClient(final int timeoutSec){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSec)
                .responseTimeout(Duration.ofMillis(timeoutSec))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSec, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSec, TimeUnit.MILLISECONDS)));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
