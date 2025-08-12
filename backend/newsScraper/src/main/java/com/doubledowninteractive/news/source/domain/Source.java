package com.doubledowninteractive.news.source.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Source {
    private Long userId;
    private Long id;
    private String code;
    private String name;
    private String baseUrl;
    private Boolean enabled;
    private LocalDateTime createdAt;

    private String collector;
    private String params;

}
