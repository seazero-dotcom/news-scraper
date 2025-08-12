package com.doubledowninteractive.news.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 안전하게 JSON 문자열에서 key 값을 꺼냄. 실패/없음 → null */
    public static String getString(String json, String key) {
        if (json == null || json.isBlank() || key == null) return null;
        try {
            JsonNode n = MAPPER.readTree(json);
            if (n == null) return null;
            String v = n.path(key).asText(null);
            return (v != null && !v.isBlank()) ? v : null;
        } catch (Exception e) {
            return null;
        }
    }
}
