package com.doubledowninteractive.news.common.exception;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외를 잡아 ApiResponse.error(...)로 변환해 반환
 * * 컨트롤러마다 try/catch를 넣는 대신, 전역(@RestControllerAdvice) 에서 한 번에 가로채서 처리
 * * 비즈니스 도메인에서 의도적으로 던지는 예외는 BusinessException을 상속받아 사용
 * * 예외가 발생하면 ApiResponse.error(...)로 변환해 반환
 * * 예외가 발생하지 않으면 ApiResponse.ok(...)로 반환
 * * 예외가 발생하면 HTTP 상태 코드와 에러 메시지를 포함한 JSON 응답을 반환
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getCode(), e.getMessage(), e.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleOthers(Exception e) {
        // 운영땐 로깅 추가
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", e.getMessage(), 500));
    }
}
