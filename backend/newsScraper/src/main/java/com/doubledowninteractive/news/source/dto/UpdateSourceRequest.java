package com.doubledowninteractive.news.source.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSourceRequest {
    private String name;
    private String baseUrl;
    private Boolean enabled;
    private String collector;
    private Map<String, Object> params;
}
