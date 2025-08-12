package com.doubledowninteractive.news.article.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 크롤링/수집된 뉴스 기사 엔티티
 * - urlHash는 URL의 SHA-256 등으로 생성해 중복 방지(UNIQUE) 용도로 사용
 * - publishedAt이 없을 수 있어 fetchedAt(수집 시각)으로 대체 정렬
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    private Long id;

    private Long sourceId;
    private Long keywordId;

    private String title;
    private String url;
    private String urlHash;

    private String lang;

    private String summary;
    private String imageUrl;

    private LocalDateTime publishedAt; // 기사 게시 시각(없으면 null)
    private LocalDateTime fetchedAt;   // 수집한 시각

}
