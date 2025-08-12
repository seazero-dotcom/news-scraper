package com.doubledowninteractive.news.article.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleListItemDto {
    private Long id;

    private Long sourceId;
    private String sourceCode;   // JOIN: sources.code
    private String sourceName;   // JOIN: sources.name

    private Long keywordId;
    private String keywordWord;  // JOIN: keywords.word (null 가능)

    private String title;
    private String url;
    private String lang;

    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;
}
