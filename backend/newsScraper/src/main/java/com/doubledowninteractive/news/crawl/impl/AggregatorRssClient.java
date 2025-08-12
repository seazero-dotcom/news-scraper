package com.doubledowninteractive.news.crawl.impl;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.common.util.HashUtils;
import com.doubledowninteractive.news.common.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

// backend/.../crawl/impl/AggregatorRssClient.java
@Slf4j
class AggregatorRssClient {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter RFC1123 = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

    private static final DateTimeFormatter RSS =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    // ✅ 기존 전체 검색(ALL) 유지
    List<Article> fetch(String query, Long sourceId) {
        return fetch(query, sourceId, null);
    }

    // ✅ site 도메인 필터가 추가된 버전 (예: news.naver.com, v.daum.net)
    List<Article> fetch(String query, Long sourceId, String siteDomain) {
        String q = (siteDomain != null && !siteDomain.isBlank())
                ? ("site:" + siteDomain + " " + query)
                : query;

        String url = "https://news.google.com/rss/search?q=" +
                java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8) +
                "&hl=ko&gl=KR&ceid=KR:ko";

        Request req = new Request.Builder().url(url).get().build();
        List<Article> out = new ArrayList<>();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return out;
            String xml = resp.body().string();
            Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
            for (Element item : doc.select("item")) {
                String title = opt(item, "title");
                String link  = opt(item, "link");
                String pub   = opt(item, "pubDate");
                if (title == null || link == null) continue;

                String norm = UrlUtils.normalize(link);
                Article a = new Article();
                a.setSourceId(sourceId);
                a.setUrl(norm);
                a.setUrlHash(HashUtils.sha256(norm));
                a.setTitle(title);
                a.setLang("ko");
                a.setPublishedAt(parse(pub));
                out.add(a);
            }
        } catch (IOException e) {
            log.warn("AggregatorRssClient error: {}", e.toString());
        }
        return out;
    }

    private static String opt(Element e, String tag) {
        var el = e.selectFirst(tag); return el != null ? el.text() : null;
    }
    private static LocalDateTime parse(String pub) {
        if (pub == null || pub.isBlank()) return LocalDateTime.now(KST);
        try {
            // "Mon, 11 Aug 2025 00:12:34 +0000" 같은 RFC1123을 타임존 보존해서 KST로 변환
            return ZonedDateTime.parse(pub, RFC1123).withZoneSameInstant(KST).toLocalDateTime();
        } catch (Exception ignore) {
            try {
                // ISO-8601 등 다른 포맷도 시도
                return ZonedDateTime.parse(pub).withZoneSameInstant(KST).toLocalDateTime();
            } catch (Exception ignored) {
                return LocalDateTime.now(KST);
            }
        }
    }
}

