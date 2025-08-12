package com.doubledowninteractive.news.crawl.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.crawl.scheduler.CrawlScheduler;
import com.doubledowninteractive.news.security.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlController {
    private final CrawlScheduler scheduler;


    @PostMapping("/trigger")
    public ApiResponse<?> trigger() {
        log.info("[crawl] HTTP trigger HIT");
        Long userId = AuthUtils.currentUserId(); // JWT에서 꺼낸 내 userId
        if (userId == null) {
            return ApiResponse.unauthorized("로그인이 필요합니다.");
        }
        scheduler.runOnce(userId);       // 사용자 한 명 기준으로 수집
        log.info("[crawl] HTTP trigger DONE");
        return ApiResponse.ok(java.util.Map.of("triggered", true));
    }
}
