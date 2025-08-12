package com.doubledowninteractive.news.article.service;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.dto.ArticleDto;
import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import com.doubledowninteractive.news.article.repository.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleMapper mapper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void upsert(Article a) {
        // ✅ DB NOW() 대신 자바에서 수집시각 세팅 (KST)
        if (a.getFetchedAt() == null) {
            a.setFetchedAt(LocalDateTime.now(KST));
        }
        mapper.insertIgnore(a); // 중복(URL 해시)일 땐 fetched_at만 업데이트
    }

    // ArticleService.java
    public long count(String q, Long sourceId, Long keywordId, LocalDate from, LocalDate to) {
        return mapper.count(q, sourceId, keywordId, from, to);
    }

    public List<ArticleDto> find(String q, Long sourceId, Long keywordId,
                                 LocalDate from, LocalDate to, String sort, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = p * s;

        List<Article> entities = mapper.find(q, sourceId, keywordId, from, to, sort, offset, s);
        List<ArticleDto> out = new ArrayList<>(entities.size());
        for (Article e : entities) {
            out.add(ArticleDto.from(e));
        }
        return out;
    }

    public List<ArticleListItemDto> findForList(String q, Long sourceId, Long keywordId,
                                                LocalDate from, LocalDate to, String sort,
                                                int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = p * s;

        return mapper.findList(q, sourceId, keywordId, from, to, sort, offset, s);
    }

}
