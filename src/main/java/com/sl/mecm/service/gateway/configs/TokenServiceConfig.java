package com.sl.mecm.service.gateway.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "mecm.gateway-service.services.token")
public class TokenServiceConfig {

    private String host;

    private String pathQuery;

    private String pathSave;

    private String pathSessionTrust;

    private String pathClientTrust;

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

    public String getPathSessionTrust() {
        return pathSessionTrust;
    }

    public void setPathSessionTrust(String pathSessionTrust) {
        this.pathSessionTrust = pathSessionTrust;
    }

    public String getPathClientTrust() {
        return pathClientTrust;
    }

    public void setPathClientTrust(String pathClientTrust) {
        this.pathClientTrust = pathClientTrust;
    }
}
