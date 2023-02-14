package com.sl.mecm.service.gateway.utils;

import com.sl.mecm.core.commons.utils.UUIDTool;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenUtils {

    private TokenUtils(){}

    public static String createAuthToken(){
        StringBuilder sb = new StringBuilder(UUIDTool.applyUUID32());
        return sb.substring(0, sb.length()/2);
    }

    public static String createAuthId(){
        StringBuilder sb = new StringBuilder(UUIDTool.applyUUID32());
        return sb.substring(0, sb.length()/2);
    }

    public static String createSessionToken(){
        return UUIDTool.applyUUID36();
    }

    public static String applyAccessToken(String authId, String authToken){
        if (StringUtils.hasText(authToken) && StringUtils.hasText(authId)){
            return Base64.getEncoder().encodeToString((authToken + "." + authId).getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }
}
