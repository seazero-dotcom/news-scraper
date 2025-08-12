package com.doubledowninteractive.news.source.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.source.domain.Source;
import com.doubledowninteractive.news.source.dto.CreateSourceRequest;
import com.doubledowninteractive.news.source.dto.SourceDto;
import com.doubledowninteractive.news.source.dto.ToggleSourceRequest;
import com.doubledowninteractive.news.source.dto.UpdateSourceRequest;
import com.doubledowninteractive.news.source.service.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private static final String ONLY_COLLECTOR = "AGGREGATOR_RSS_SITE";

    private final SourceService service;

    @GetMapping
    public ApiResponse<List<SourceDto>> list() {
        List<Source> rows = service.findAll();
        List<SourceDto> out = new ArrayList<>(rows.size());
        for (Source s : rows) out.add(SourceDto.of(s));
        return ApiResponse.ok(out, Map.of("count", out.size()));
    }

    @PostMapping
    public ApiResponse<?> add(@Valid @RequestBody CreateSourceRequest req) {
        // collector는 단일 방식만 허용
        String collector = ONLY_COLLECTOR;

        // params.site 필수 체크
        Map<String, Object> params = req.getParams();
        if (params == null || !params.containsKey("site")
                || String.valueOf(params.get("site")).trim().isEmpty()) {
            throw new IllegalArgumentException("params.site is required (e.g. news.naver.com)");
        }

        service.add(
                req.getCode(),
                req.getName(),
                req.getBaseUrl(),
                collector,
                params
        );
        return ApiResponse.ok(Map.of("created", true));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<?> toggle(@PathVariable Long id,
                                 @Valid @RequestBody ToggleSourceRequest req) {
        service.toggle(id, Boolean.TRUE.equals(req.getEnabled()));
        return ApiResponse.ok(Map.of("updated", true));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateSourceRequest req) {
        // collector는 서비스에서 AGGREGATOR_RSS_SITE로 강제됨.
        // params가 온 경우 site 필수 검증(서비스에서도 한 번 더 검증)
        Map<String, Object> params = req.getParams();
        if (params != null) {
            Object site = params.get("site");
            if (site == null || String.valueOf(site).trim().isEmpty()) {
                throw new IllegalArgumentException("params.site is required (e.g. news.naver.com)");
            }
        }

        service.update(
                id,
                req.getName(),
                req.getBaseUrl(),
                req.getEnabled(),     // ✅ enabled도 함께 반영
                req.getCollector(),   // 무시되더라도 시그니처 맞춤 (서비스에서 강제)
                params
        );
        return ApiResponse.ok(Map.of("updated", true));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean force) {
        service.remove(id, force);
        return ApiResponse.ok(Map.of("deleted", true, "force", force));
    }
}
