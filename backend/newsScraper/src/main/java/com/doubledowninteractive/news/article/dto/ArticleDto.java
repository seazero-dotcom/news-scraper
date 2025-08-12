package com.doubledowninteractive.news.article.dto;

import com.doubledowninteractive.news.article.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private Long sourceId;
    private Long keywordId;
    private String title;
    private String url;
    private String lang;
    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;

    // 변환 메서드
    public static ArticleDto from(Article a) {
        if (a == null) return null;
        ArticleDto d = new ArticleDto();
        d.setId(a.getId());
        d.setSourceId(a.getSourceId());
        d.setKeywordId(a.getKeywordId());
        d.setTitle(a.getTitle());
        d.setUrl(a.getUrl());
        d.setLang(a.getLang());
        d.setPublishedAt(a.getPublishedAt());
        d.setFetchedAt(a.getFetchedAt());
        return d;
    }
}
