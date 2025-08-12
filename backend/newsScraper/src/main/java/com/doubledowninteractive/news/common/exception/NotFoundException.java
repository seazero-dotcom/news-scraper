package com.doubledowninteractive.news.common.exception;

/**
 * 요청한 리소스가 존재하지 않을 때 던지는 예외.
 * 예: 잘못된 URL, DB에 없는 데이터 요청 등.
 * 404 Not Found 상태 코드로 응답.
 */
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message, 404);
    }
}
