package com.doubledowninteractive.news.crawl.impl;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.crawl.service.CollectorService;
import com.doubledowninteractive.news.source.domain.Source;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.doubledowninteractive.news.common.util.JsonUtils.getString;

@Component("AGGREGATOR_RSS_SITE")
public class AggregatorRssSiteCollector implements CollectorService {

    private final AggregatorRssClient rss = new AggregatorRssClient(); // 오버로드(fetch(query, sourceId, site)) 버전

    @Override
    public List<Article> collect(Source src, String keyword) {
        // ✅ params가 String(JSON)이든 Map이든 안전하게 꺼냄
        String site = getString(src.getParams(), "site"); // 예: "news.naver.com" / "v.daum.net"
        return rss.fetch(keyword, src.getId(), site);
    }
}
