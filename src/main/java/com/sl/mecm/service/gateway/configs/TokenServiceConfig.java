package com.sl.mecm.auth.intercptor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mecm.auth.intercptor.token")
public class TokenServiceConfig {

    private String host;

    private String pathQuery;

    private String pathSave;

    private int timeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPathQuery() {
        return pathQuery;
    }

    public void setPathQuery(String pathQuery) {
        this.pathQuery = pathQuery;
    }

    public String getPathSave() {
        return pathSave;
    }

    public void setPathSave(String pathSave) {
        this.pathSave = pathSave;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
