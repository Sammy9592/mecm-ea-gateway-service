package com.sl.mecm.service.gateway.exceptions;

public class AuthException extends RuntimeException{

    private String code;
    private String message;
    private Object details;

    public AuthException( String code, String message, Object details) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public AuthException(String code, String message, Object details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }
}
