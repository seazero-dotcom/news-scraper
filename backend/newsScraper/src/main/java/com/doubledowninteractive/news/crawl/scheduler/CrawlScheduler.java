package com.doubledowninteractive.news.crawl.scheduler;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.service.ArticleService;
import com.doubledowninteractive.news.crawl.service.CollectorService;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.repository.KeywordMapper;
import com.doubledowninteractive.news.source.domain.Source;
import com.doubledowninteractive.news.source.service.SourceService;
import com.doubledowninteractive.news.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 매시 정각에 (소스 × 키워드) 조합으로 뉴스 수집을 병렬 실행하는 스케줄러.
 * - 수집기(CollectorService)를 타입명으로 찾아 실행
 * - 각 작업에 타임아웃과 간단한 재시도 적용
 * - 수집 결과는 upsert(있으면 갱신, 없으면 삽입)
 *
 * @EnableScheduling 메인 클래스에 붙어 있어야 스케줄이 돈다.
 * CrawlExecutorConfig에서 @Bean(name = "crawlExecutor")로 스레드풀이 등록돼 있어야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {

    // ====== 서비스 의존성 주입 ======
    private final KeywordMapper keywordMapper;
    private final SourceService sourceService;
    private final ArticleService articleService;
    private final UserService userService;

    /**
     * collector 타입명(String) → 구현체(CollectorService) 매핑
     * → 각 Collector 구현 클래스에 @Component("AGGREGATOR_RSS_SITE") 처럼 빈 이름을 collector 값과 정확히 맞춰야 함
     */
    private final Map<String, CollectorService> collectors;
    /** 병렬 실행을 위한 스레드풀*/
    private final @Qualifier("crawlExecutor") Executor crawlExecutor;
    /** 각 (소스×키워드) 작업의 최대 허용 시간(ms) */
    private static final long TASK_TIMEOUT_MILLIS = 15_000L;
    /** 실패 시 재시도 횟수. 0이면 재시도 안 함 */
    private static final int  RETRIES = 1;
    /** 재시도 사이 대기 */
    private static final long RETRY_BACKOFF_MS = 500L;
    // ============================================

    /**
     * 매시 정각 자동 실행
     * cron: 초 분 시 일 월 요일
     * "0 0 * * * *" 는 "매시 0분 0초"
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void hourly() {
        for (Long uid : userService.findAllIds()) {
            this.runOnce(uid);  // 각 사용자별 실행
        }
    }


    public void runOnce(Long userId) {

        List<Source> sources  = Optional.ofNullable(sourceService.findAllEnabled(userId)).orElseGet(List::of);
        List<Keyword> keywords = Optional.ofNullable(keywordMapper.findAllEnabled(userId)).orElseGet(List::of);

        log.info("[crawl] trigger: enabled sources={}, enabled keywords={}", sources.size(), keywords.size());

        if (sources.isEmpty() || keywords.isEmpty()) {
            log.info("[crawl] skip: sources={}, keywords={}", sources.size(), keywords.size());
            return;
        }

        long t0 = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Source s : sources) {
            String type = s.getCollector();
            CollectorService collector = collectors.get(type);
            if (collector == null) {
                log.warn("[crawl] collector bean not found: type={}, sourceCode={}", type, s.getCode());
                continue;
            }
            for (Keyword k : keywords) {
                final String kw = k.getWord();
                CompletableFuture<Void> f = CompletableFuture
                        .supplyAsync(() -> tryCollect(collector, s, kw), crawlExecutor)
                        .completeOnTimeout(Collections.emptyList(), TASK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .handle((items, ex) -> {
                            if (ex != null) {
                                log.warn("[crawl] collect failed type={} srcCode={} kw='{}' err={}",
                                        type, s.getCode(), kw, ex.getMessage());
                                return null;
                            }
                            int saved = 0;
                            if (items != null) {
                                for (Article a : items) {
                                    try {
                                        a.setKeywordId(k.getId());
                                        articleService.upsert(a);
                                        saved++;
                                    } catch (Exception e) {
                                        log.debug("[crawl] upsert fail url={} err={}", a.getUrl(), e.getMessage());
                                    }
                                }
                            }
                            log.info("[crawl] saved type={} srcCode={} kw='{}' saved={}", type, s.getCode(), kw, saved);
                            return null;
                        });
                futures.add(f);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("[crawl] done: tasks={} took={}ms", futures.size(), (System.currentTimeMillis() - t0));
    }

    /**
     * 새 키워드가 추가된 직후, 그 키워드만 1회 즉시 수집
     */
    public void runOnceForNewKeyword(Long userId, String keywordWord, Long keywordId) {
        var sources = sourceService.findAllEnabled(userId);
        if (sources == null || sources.isEmpty()) {
            log.info("[crawl:new-keyword] skip: no enabled sources");
            return;
        }
        long t0 = System.currentTimeMillis();
        int totalSaved = 0;

        for (var s : sources) {
            var collector = collectors.get(s.getCollector());
            if (collector == null) {
                log.warn("[crawl:new-keyword] collector not found: type={}, sourceCode={}", s.getCollector(), s.getCode());
                continue;
            }
            List<Article> items = tryCollect(collector, s, keywordWord);
            int saved = 0;
            if (items != null) {
                for (Article a : items) {
                    try {
                        a.setKeywordId(keywordId);
                        articleService.upsert(a);
                        saved++;
                    } catch (Exception e) {
                        log.debug("[crawl:new-keyword] upsert fail url={} err={}", a.getUrl(), e.getMessage());
                    }
                }
            }
            totalSaved += saved;
            log.info("[crawl:new-keyword] saved type={} srcCode={} kw='{}' saved={}", s.getCollector(), s.getCode(), keywordWord, saved);
        }
        log.info("[crawl:new-keyword] done: kw='{}' totalSaved={} took={}ms", keywordWord, totalSaved, (System.currentTimeMillis() - t0));
    }

    /**
     * 재시도
     */
    private List<Article> tryCollect(CollectorService c, Source s, String kw) {
        int attempt = 0;
        while (true) {
            attempt++;
            long start = System.currentTimeMillis();
            try {
                List<Article> items = Optional.ofNullable(c.collect(s, kw)).orElseGet(List::of);
                long took = System.currentTimeMillis() - start;
                log.debug("[crawl] collected type={} srcCode={} kw='{}' count={} in {}ms (attempt={})",
                        s.getCollector(), s.getCode(), kw, items.size(), took, attempt);
                return items;
            } catch (Exception e) {
                if (attempt > Math.max(0, RETRIES)) throw e;
                sleepQuiet(RETRY_BACKOFF_MS);
            }
        }
    }
    /**
     * 주어진 시간(ms) 동안 조용히 대기
     * InterruptedException 발생 시 현재 스레드의 인터럽트 상태를 복원
     */
    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
