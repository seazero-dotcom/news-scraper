package com.doubledowninteractive.news.keyword.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleKeywordRequest {
    @NotNull
    private Boolean enabled;
}
