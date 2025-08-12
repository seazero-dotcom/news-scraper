package com.doubledowninteractive.news.source.dto;

import com.doubledowninteractive.news.source.domain.Source;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class SourceDto {
    private Long id;
    private String code;
    private String name;
    private String baseUrl;
    private Boolean enabled;

    public static SourceDto of(Source s) {
        if (s == null) return null;
        return new SourceDto(s.getId(), s.getCode(), s.getName(), s.getBaseUrl(), s.getEnabled());
    }
}
