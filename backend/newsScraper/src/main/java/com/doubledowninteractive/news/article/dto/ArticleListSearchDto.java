package com.doubledowninteractive.news.article.dto;

import lombok.Data;

@Data
public class ArticleListSearchDto {
    private String q;          // 검색어
    private Long sourceId;     // 소스 ID (null 가능)
    private Long keywordId;    // 키워드 ID (null 가능)
    private String from;       // 시작 날짜 (yyyy-MM-dd 형식, null 가능)
    private String to;         // 종료 날짜 (yyyy-MM-dd 형식, null 가능)
    private String sort;       // 정렬 기준 (예: "publishedAt", "fetchedAt")
    private int offset ;    // 페이지 오프셋
    private int size ;     // 페이지 크기

}
