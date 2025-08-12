package com.doubledowninteractive.news.article.service;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import com.doubledowninteractive.news.article.repository.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleMapper mapper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void upsert(Article a) {
        if (a.getFetchedAt() == null) {
            a.setFetchedAt(LocalDateTime.now(KST));
        }
        mapper.insertIgnore(a);
    }

    public long count(Long userId, String q, Long sourceId, Long keywordId, LocalDate from, LocalDate to) {
        return mapper.count(userId, q, sourceId, keywordId, from, to);
    }

    public List<ArticleListItemDto> findForList(Long userId, String q, Long sourceId, Long keywordId,
                                                LocalDate from, LocalDate to, String sort,
                                                int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = p * s;

        return mapper.findList(userId, q, sourceId, keywordId, from, to, sort, offset, s);
    }
}
