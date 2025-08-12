package com.doubledowninteractive.news.common.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 뉴스 URL에는 utm_*, gclid, fbclid 같은 추적용 파라미터가 많이 붙음.
 * 기사 본문은 동일하지만 링크만 다르면 중복이 생김 → 이들을 제거해서 “같은 기사 = 같은 URL”이 되도록 만듦.
 * https://example.com/path?utm_source=google → https://example.com/path
 */
public class UrlUtils {
    public static String normalize(String raw) {
        try {
            URI u = new URI(raw);
            String query = u.getQuery();
            if (query != null && query.toLowerCase().contains("utm_")) query = null;
            String path = (u.getPath() == null || u.getPath().isEmpty()) ? "/" : u.getPath();
            return new URI(u.getScheme(), u.getAuthority(), path, query, null).toString();
        } catch (URISyntaxException e) { return raw; }
    }
}
