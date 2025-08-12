package com.doubledowninteractive.news.keyword.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.dto.CreateKeywordRequest;
import com.doubledowninteractive.news.keyword.dto.KeywordDto;
import com.doubledowninteractive.news.keyword.dto.ToggleKeywordRequest;
import com.doubledowninteractive.news.keyword.service.KeywordService;
import com.doubledowninteractive.news.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<KeywordDto>> list(Authentication authentication) {
        long userId = userService.currentUserId(authentication);
        List<Keyword> rows = keywordService.findAllByUser(userId);
        List<KeywordDto> out = new ArrayList<>(rows.size());
        for (Keyword k : rows) out.add(KeywordDto.of(k));
        return ApiResponse.ok(out, Map.of("count", out.size()));
    }

    @PostMapping
    public ApiResponse<?> add(Authentication authentication,
                              @Valid @RequestBody CreateKeywordRequest req) {
        long userId = userService.currentUserId(authentication);
        // 최초 등록 1회 즉시 수집 수행
        keywordService.add(userId, req.getWord());
        return ApiResponse.ok(Map.of("created", true));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> toggle(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody ToggleKeywordRequest req) {
        long userId = userService.currentUserId(authentication);
        boolean enabled = Boolean.TRUE.equals(req.getEnabled());
        keywordService.toggle(userId, id, enabled);
        return ApiResponse.ok(Map.of("updated", true));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(Authentication authentication,
                                 @PathVariable Long id) {
        long userId = userService.currentUserId(authentication);
        keywordService.remove(userId, id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
