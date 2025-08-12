package com.doubledowninteractive.news.article.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleListItemDto {
    private Long id;

    private Long sourceId;
    private String sourceCode;
    private String sourceName;

    private Long keywordId;
    private String keywordWord;

    private String title;
    private String url;
    private String lang;

    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;

    private Long userId;
}
