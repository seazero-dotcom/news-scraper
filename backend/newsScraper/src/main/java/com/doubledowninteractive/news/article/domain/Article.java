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

    private Long sourceId;     // FK: sources.id
    private Long keywordId;    // FK: keywords.id (nullable, 없을 수 있음)

    private String title;
    private String url;
    private String urlHash;    // UNIQUE 인덱스 권장

    private String lang;       // 예: "ko", "en"

    // 🔽 추가
    private String summary;   // NULL 가능
    private String imageUrl;  // NULL 가능

    private LocalDateTime publishedAt; // 기사 게시 시각(없으면 null)
    private LocalDateTime fetchedAt;   // 우리 시스템이 수집한 시각

    // 선택(스키마에 있으면 매핑): 생성/수정 시각
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
