package com.doubledowninteractive.news.source.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class CreateSourceRequest {
    @NotBlank private String code;     // 예: BBC
    @NotBlank private String name;     // 예: BBC News
    @NotBlank private String baseUrl;  // 예: https://www.bbc.com

    // 선택값 (비우면 컨트롤러에서 AGGREGATOR_RSS 기본값으로 처리)
    private String collector;          // AGGREGATOR_RSS | AGGREGATOR_RSS_SITE | DIRECT_RSS
    private Map<String, Object> params; // 예: { "site": "bbc.com" } 또는 { "rssUrl": "..." }
}
