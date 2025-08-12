package com.doubledowninteractive.news.common.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 뉴스 URL에는 utm_*, gclid, fbclid 같은 추적용 파라미터가 자주 붙어.
 * 기사 본문은 동일하지만 링크만 다르면 중복이 생김 → 이들을 제거해서 “같은 기사 = 같은 URL”이 되도록 만듦.
 * 또한 경로가 비어있으면 /로, fragment(앵커 #...)는 제거.
 * 예: https://example.com/path?utm_source=google → https://example.com/path
 * * 주의: 이 메서드는 URL을 정규화할 뿐, 유효성을 검사하지 않음. 잘못된 URL은 URISyntaxException이 발생할 수 있음.
 */
public class UrlUtils {
    public static String normalize(String raw) {
        try {
            URI u = new URI(raw);
            String query = u.getQuery();
            // (간단) 추적 파라미터 포함되면 쿼리 제거
            if (query != null && query.toLowerCase().contains("utm_")) query = null;
            String path = (u.getPath() == null || u.getPath().isEmpty()) ? "/" : u.getPath();
            return new URI(u.getScheme(), u.getAuthority(), path, query, null).toString();
        } catch (URISyntaxException e) { return raw; }
    }
}
