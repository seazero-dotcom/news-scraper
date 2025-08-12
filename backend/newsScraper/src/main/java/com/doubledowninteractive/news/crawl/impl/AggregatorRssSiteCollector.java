package com.doubledowninteractive.news.crawl.impl;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.crawl.service.CollectorService;
import com.doubledowninteractive.news.source.domain.Source;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.doubledowninteractive.news.common.util.JsonUtils.getString;

@Component("AGGREGATOR_RSS_SITE")
public class AggregatorRssSiteCollector implements CollectorService {

    private final AggregatorRssClient rss = new AggregatorRssClient();

    @Override
    public List<Article> collect(Source src, String keyword) {
        String site = getString(src.getParams(), "site"); // "news.naver.com" / "v.daum.net"
        return rss.fetch(keyword, src.getId(), site);
    }
}
