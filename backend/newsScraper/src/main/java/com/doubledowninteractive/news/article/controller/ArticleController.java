package com.doubledowninteractive.news.article.controller;

import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import com.doubledowninteractive.news.article.service.ArticleService;
import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<List<ArticleListItemDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) Long keywordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100); // 최대 100개로 제한

        Long userId = AuthUtils.currentUserId(); // JWT에서 꺼낸 내 userId
        if (userId == null) {
            userId = 0L; // 인증되지 않은 경우 0으로 처리
        }

        long total = articleService.count(userId, q, sourceId, keywordId, from, to);

        List<ArticleListItemDto> rows = articleService.findForList(
                userId, q, sourceId, keywordId, from, to, sort, safePage, safeSize
        );

        int totalPages = (int) Math.ceil((double) total / safeSize);
        boolean hasNext = (long) (safePage + 1) * safeSize < total;

        return ApiResponse.ok(rows, Map.of(
                "page", safePage,
                "size", safeSize,
                "total", total,
                "totalPages", totalPages,
                "hasNext", hasNext,
                "nextPage", hasNext ? safePage + 1 : safePage,
                "sort", sort
        ));
    }

}
