package com.sl.mecm.service.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.sl.mecm.*")
@EnableAutoConfiguration(exclude = org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
@Slf4j
public class GatewayServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(GatewayServiceApplication.class, args);
        }catch (Exception e){
            log.error("start app error", e);
            e.printStackTrace();
        }
    }
}