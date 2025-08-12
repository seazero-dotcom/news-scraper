package com.doubledowninteractive.news.crawl.service;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.source.domain.Source;

import java.util.List;

public interface CollectorService {
    List<Article> collect(Source src, String keyword);
}

