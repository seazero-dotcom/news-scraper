package com.doubledowninteractive.news.keyword.dto;

import com.doubledowninteractive.news.keyword.domain.Keyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class KeywordDto {
    private Long id;
    private String word;
    private Boolean enabled;

    public static KeywordDto of(Keyword k) {
        if (k == null) return null;
        return new KeywordDto(k.getId(), k.getWord(), k.getEnabled());
    }
}
