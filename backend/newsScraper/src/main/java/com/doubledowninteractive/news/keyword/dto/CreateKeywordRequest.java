package com.doubledowninteractive.news.keyword.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateKeywordRequest {
    @NotBlank
    private String word;
}
