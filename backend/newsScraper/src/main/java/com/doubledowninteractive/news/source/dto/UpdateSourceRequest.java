package com.doubledowninteractive.news.source.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 소스 부분 수정용 DTO: null 필드는 변경하지 않음 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSourceRequest {
    private String name;       // 예: "BBC News (KR)"
    private String baseUrl;    // 예: "https://www.bbc.com"
    private Boolean enabled;
    private String collector;  // AGGREGATOR_RSS | AGGREGATOR_RSS_SITE | DIRECT_RSS
    private Map<String, Object> params; // collector별 설정 (site/rssUrl 등)
}
