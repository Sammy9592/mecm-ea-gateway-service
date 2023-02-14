package com.sl.mecm.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class GatewayServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceApplication.class);

    public static void main(String[] args) {
        try {
            logger.debug("start web service application debug");
            logger.info("start web service application info");
            logger.error("start web service application error");
            SpringApplication.run(GatewayServiceApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}