package com.doubledowninteractive.news.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
    * 유틸리티 클래스: 문자열을 SHA-256 해시로 변환하는 메서드 제공.
    *
 */
public class HashUtils {
    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
