package com.doubledowninteractive.news.common.exception;

public class DuplicateException extends BusinessException {
    public DuplicateException(String message) {
        super("DUPLICATE_KEYWORD", message, 409); // HTTP 409 Conflict
    }
}
