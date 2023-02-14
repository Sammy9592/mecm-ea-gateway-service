package com.sl.mecm.service.user.idv.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mecm.user-idv.services.cache")
public class CacheServiceConfigs {

    private String host;

    private String endpointSaveCache;

    private int timeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEndpointSaveCache() {
        return endpointSaveCache;
    }

    public void setEndpointSaveCache(String endpointSaveCache) {
        this.endpointSaveCache = endpointSaveCache;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
