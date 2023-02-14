package com.sl.mecm.service.gateway.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mecm.gateway-service.services.cache")
public class CacheServiceConfigs {

    private String host;

    private String pathQueryCache;

    private int timeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPathQueryCache() {
        return pathQueryCache;
    }

    public void setPathQueryCache(String pathQueryCache) {
        this.pathQueryCache = pathQueryCache;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
