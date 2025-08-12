package com.doubledowninteractive.news.crawl.controller;

import com.doubledowninteractive.news.common.dto.ApiResponse;
import com.doubledowninteractive.news.crawl.scheduler.CrawlScheduler;
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
        scheduler.runOnce();
        log.info("[crawl] HTTP trigger DONE");
        return ApiResponse.ok(java.util.Map.of("triggered", true));
    }
}
