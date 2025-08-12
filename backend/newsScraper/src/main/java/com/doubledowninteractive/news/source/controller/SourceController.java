package com.doubledowninteractive.news.source.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.source.domain.Source;
import com.doubledowninteractive.news.source.dto.CreateSourceRequest;
import com.doubledowninteractive.news.source.dto.SourceDto;
import com.doubledowninteractive.news.source.dto.ToggleSourceRequest;
import com.doubledowninteractive.news.source.dto.UpdateSourceRequest;
import com.doubledowninteractive.news.source.service.SourceService;
import com.doubledowninteractive.news.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService service;
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<SourceDto>> list(Authentication authentication) {
        long userId = userService.currentUserId(authentication);
        List<Source> rows = service.findAll(userId);
        List<SourceDto> out = new ArrayList<>(rows.size());
        for (Source s : rows) out.add(SourceDto.of(s));
        return ApiResponse.ok(out, Map.of("count", out.size()));
    }

    @PostMapping
    public ApiResponse<?> add(Authentication authentication, @Valid @RequestBody CreateSourceRequest req) {
        long userId = userService.currentUserId(authentication);
        // params.site 필수 체크
        var params = req.getParams();
        if (params == null || !params.containsKey("site")
                || String.valueOf(params.get("site")).trim().isEmpty()) {
            throw new IllegalArgumentException("params.site is required (e.g. news.naver.com)");
        }

        Boolean enabled = req.getEnabled();

        service.add(userId,
                req.getCode(),
                req.getName(),
                req.getBaseUrl(),
                enabled,
                params
        );
        return ApiResponse.ok(Map.of("created", true));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<?> toggle(Authentication authentication, @PathVariable Long id,
                                 @Valid @RequestBody ToggleSourceRequest req) {
        long userId = userService.currentUserId(authentication);
        service.toggle(userId, id, Boolean.TRUE.equals(req.getEnabled()));
        return ApiResponse.ok(Map.of("updated", true));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> update(Authentication authentication, @PathVariable Long id,
                                 @Valid @RequestBody UpdateSourceRequest req) {
        long userId = userService.currentUserId(authentication);
        var params = req.getParams();
        if (params != null) {
            Object site = params.get("site");
            if (site == null || String.valueOf(site).trim().isEmpty()) {
                throw new IllegalArgumentException("params.site is required (e.g. news.naver.com)");
            }
        }

        service.update(
                userId,
                id,
                req.getName(),
                req.getBaseUrl(),
                req.getEnabled(),
                req.getCollector(),
                params
        );
        return ApiResponse.ok(Map.of("updated", true));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(Authentication authentication, @PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean force) {
        long userId = userService.currentUserId(authentication);
        service.remove(userId, id, force);
        return ApiResponse.ok(Map.of("deleted", true, "force", force));
    }
}
