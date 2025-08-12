package com.doubledowninteractive.news.common.exception;

/**
 * 컨트롤러마다 try/catch를 넣는 대신, 전역(@RestControllerAdvice) 에서 한 번에 가로채서 ApiResponse.error(...)로 내려주면 깔끔함.
 * 비즈니스 도메인에서 의도저긍로 던지는 예외.
 */
public class BusinessException extends RuntimeException {
    private final String code;
    private final int status;

    public BusinessException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }
    public String getCode() { return code; }
    public int getStatus() { return status; }
}
