package com.doubledowninteractive.news.keyword.domain;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Keyword {
    private Long id;
    private String word;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
