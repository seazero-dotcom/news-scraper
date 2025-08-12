package com.doubledowninteractive.news.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 컨트롤러마다 제각각 JSON을 리턴하면 프론트가 처리하기 힘들어져. 하나의 포맷으로 통일하기 위한 DTO.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success; // 성공 여부
    private T data; // 성공 시 데이터
    private Map<String,Object> meta; // 페이징 등 부가 정보
    private ErrorBody error; // 실패 시 에러 정보

    public static <T> ApiResponse<T> ok(T data) { // 성공 응답
        return new ApiResponse<>(true, data, null, null);
    }
    public static <T> ApiResponse<T> ok(T data, Map<String, Object> meta) { // 성공 응답 (부가 정보 포함)
        return new ApiResponse<>(true, data, meta, null);
    }
    public static <T> ApiResponse<T> error(String code, String message, Integer status) { // 실패 응답
        return new ApiResponse<>(false, null, null, new ErrorBody(code, message, status));
    }

    public static ApiResponse<?> unauthorized(String s) {
        return error("UNAUTHORIZED", s, 401); // 인증 실패 응답
    }

    public record ErrorBody(String code, String message, Integer status) {} // 에러 정보 DTO
}
