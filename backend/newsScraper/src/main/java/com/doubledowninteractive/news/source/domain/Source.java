package com.doubledowninteractive.news.source.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Source {
    private Long id;
    private String code;
    private String name;
    private String baseUrl;
    private Boolean enabled;
    private LocalDateTime createdAt;

    // ✅ collector/params 반드시 있어야 함
    private String collector; // AGGREGATOR_RSS | AGGREGATOR_RSS_SITE | DIRECT_RSS
    private String params;    // DB JSON을 그대로 String으로 받음
}
