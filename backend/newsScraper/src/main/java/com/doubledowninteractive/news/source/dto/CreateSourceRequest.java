package com.doubledowninteractive.news.source.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class CreateSourceRequest {
    @NotBlank private String code;     // 예: BBC
    @NotBlank private String name;     // 예: BBC News
    @NotBlank private String baseUrl;  // 예: https://www.bbc.com

    private String collector;          // AGGREGATOR_RSS_SITE
    private Map<String, Object> params; // 예: { "site": "bbc.com" } 또는 { "rssUrl": "..." }
    private Boolean enabled;           // true/false (null이면 컨트롤러에서 true로 처리)
}
