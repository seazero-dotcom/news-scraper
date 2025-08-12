package com.doubledowninteractive.news.source.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleSourceRequest {
    @NotNull
    private Boolean enabled;
}
