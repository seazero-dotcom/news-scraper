package com.doubledowninteractive.news.common.exception;

public class ResourceInUseException extends BusinessException {
    public ResourceInUseException(String message) {
        super("RESOURCE_IN_USE", message, 409);
    }
}
