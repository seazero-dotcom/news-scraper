package com.doubledowninteractive.news.article.controller;

import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import com.doubledowninteractive.news.article.service.ArticleService;
import com.doubledowninteractive.news.common.dto.ApiResponse;
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
        int safeSize = Math.min(Math.max(1, size), 100);  // ✅ 1~100 사이로 캡

        long total = articleService.count(q, sourceId, keywordId, from, to);

        // ✅ 반드시 safeSize로 호출 (캡이 쿼리에 반영되도록)
        List<ArticleListItemDto> rows = articleService.findForList(
                q, sourceId, keywordId, from, to, sort, safePage, safeSize
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
