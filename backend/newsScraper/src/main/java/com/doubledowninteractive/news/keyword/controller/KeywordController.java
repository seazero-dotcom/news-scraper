package com.doubledowninteractive.news.keyword.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.dto.CreateKeywordRequest;
import com.doubledowninteractive.news.keyword.dto.KeywordDto;
import com.doubledowninteractive.news.keyword.dto.ToggleKeywordRequest;
import com.doubledowninteractive.news.keyword.service.KeywordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public ApiResponse<List<KeywordDto>> list() {
        List<Keyword> rows = keywordService.findAll();
        List<KeywordDto> out = new ArrayList<>(rows.size());
        for (Keyword k : rows) out.add(KeywordDto.of(k));
        return ApiResponse.ok(out, Map.of("count", out.size()));
    }

    @PostMapping
    public ApiResponse<?> add(@Valid @RequestBody CreateKeywordRequest req) {
        keywordService.add(req.getWord());
        return ApiResponse.ok(Map.of("created", true));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> toggle(@PathVariable Long id, @Valid @RequestBody ToggleKeywordRequest req) {
        keywordService.toggle(id, req.getEnabled());
        return ApiResponse.ok(Map.of("updated", true));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(@PathVariable Long id) {
        keywordService.remove(id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
