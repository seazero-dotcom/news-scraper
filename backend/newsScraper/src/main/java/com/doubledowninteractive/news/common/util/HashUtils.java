package com.doubledowninteractive.news.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 같은 기사를 여러 소스/리다이렉트로 만나도 정규화된 URL을 SHA-256으로 해시해서 articles.url_hash에 유니크 인덱스를 걸면 중복 저장을 막을 수 있어.
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
