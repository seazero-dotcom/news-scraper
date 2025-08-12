package com.doubledowninteractive.news.crawl.scheduler;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.service.ArticleService;
import com.doubledowninteractive.news.crawl.service.CollectorService;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.service.KeywordService;
import com.doubledowninteractive.news.source.domain.Source;
import com.doubledowninteractive.news.source.service.SourceService;
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
 * 각 수집기 구현에 빈 이름이 collector 값과 동일해야 한다. 예)
 * @Component("AGGREGATOR_RSS"), @Component("AGGREGATOR_RSS_SITE"), @Component("DIRECT_RSS")
 * DB articles 테이블에 keyword_id 컬럼과 FK/인덱스가 있어야 한다. (이미 추가했음)
 * ArticleService.upsert(...) SQL에 keyword_id가 포함돼야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {

    // ====== 서비스 의존성 주입 ======
    private final KeywordService keywordService; // 활성 키워드 조회
    private final SourceService sourceService;   // 활성 소스 조회
    private final ArticleService articleService; // 기사 저장(upsert)

    /**
     * collector 타입명(String) → 구현체(CollectorService) 매핑.
     * 스프링이 같은 타입의 빈들을 Map 형태로 주입해줌.
     * → 각 Collector 구현 클래스에 @Component("AGGREGATOR_RSS") 처럼
     *    "빈 이름"을 collector 값과 정확히 맞춰야 함.
     */
    private final Map<String, CollectorService> collectors;

    /**
     * 병렬 실행을 위한 스레드풀.
     * CrawlExecutorConfig 에서 @Bean(name="crawlExecutor") 로 등록된 풀을 주입받음.
     * 타입을 Executor로 받는 이유: 구현 교체(뭐가 오든) 자유롭게 하려고.
     */
    private final @Qualifier("crawlExecutor") Executor crawlExecutor;

    // ===== 튜닝 상수 (필요 시 숫자만 바꾸면 됨) =====
    /** 각 (소스×키워드) 작업의 최대 허용 시간(ms). 초과 시 그 작업은 빈 결과로 간주하고 넘어감. */
    private static final long TASK_TIMEOUT_MILLIS = 15_000L;

    /** 실패 시 재시도 횟수. 0이면 재시도 안 함. (네트워크 흔들림 등 일시 에러 보정용) */
    private static final int  RETRIES             = 1;

    /** 재시도 사이 대기(ms). 너무 짧으면 폭주, 너무 길면 전체가 느려짐. */
    private static final long RETRY_BACKOFF_MS    = 500L;
    // ============================================

    /**
     * 매시 정각(서울 시간대) 자동 실행.
     * cron: 초 분 시 일 월 요일 → "0 0 * * * *" 는 "매시 0분 0초"
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void hourly() {
        runOnce();
    }

    /**
     * 수동 트리거(예: /api/crawl/trigger)에서도 이 메서드를 호출.
     * 1) 활성 소스·키워드 조회
     * 2) (소스×키워드) 만큼 비동기 태스크 생성
     * 3) 모든 태스크 완료까지 대기
     */
    public void runOnce() {
        // NPE 방지 + 비어있으면 skip
        List<Source> sources  = Optional.ofNullable(sourceService.findAllEnabled()).orElseGet(List::of);
        List<Keyword> keywords = Optional.ofNullable(keywordService.findAllEnabled()).orElseGet(List::of);

        // ✅ 추가: 트리거 시 현재 상태를 한 줄로
        log.info("[crawl] trigger: enabled sources={}, enabled keywords={}", sources.size(), keywords.size());

        if (sources.isEmpty() || keywords.isEmpty()) {
            log.info("[crawl] skip: sources={}, keywords={}", sources.size(), keywords.size());
            return;
        }

        long t0 = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 모든 소스에 대해
        for (Source s : sources) {
            // 소스가 지정한 수집기 타입명으로 구현체 찾기
            String type = s.getCollector();
            CollectorService collector = collectors.get(type);
            if (collector == null) {
                log.warn("[crawl] collector bean not found: type={}, sourceCode={}", type, s.getCode());
                continue;
            }

            // 각 키워드별로 수집 태스크 생성
            for (Keyword k : keywords) {
                final String kw = k.getWord(); // 람다 캡처용 파이널
                log.debug("[crawl] queue task type={} srcCode={} kw='{}'", type, s.getCode(), kw);
                // supplyAsync: 별도 스레드에서 tryCollect 실행 → List<Article> 반환
                CompletableFuture<Void> f = CompletableFuture
                        .supplyAsync(() -> tryCollect(collector, s, kw), crawlExecutor)
                        // 작업이 너무 오래 걸리면 타임아웃 처리(빈 리스트 반환)
                        .completeOnTimeout(Collections.emptyList(), TASK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        // 수집된 기사들을 DB에 upsert
                        // 성공/예외/타임아웃 모두 여기서 처리
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
                                        a.setKeywordId(k.getId());              // ✅ keyword_id 저장
                                        articleService.upsert(a);
                                        saved++;
                                    } catch (Exception e) {
                                        log.debug("[crawl] upsert fail url={} err={}", a.getUrl(), e.getMessage());
                                    }
                                }
                            }
                            log.info("[crawl] saved type={} srcCode={} kw='{}' saved={}", type, s.getCode(), kw, saved);
                            return null; // Void
                        });

                futures.add(f); // 전체 완료 대기를 위해 모아둠
            }
        }

        // 모든 (소스×키워드) 태스크가 끝날 때까지 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("[crawl] done: tasks={} took={}ms", futures.size(), (System.currentTimeMillis() - t0));
    }

    /**
     * 수집 실행 + 간단 재시도.
     * - CollectorService.collect(...) 가 예외를 던지면 재시도
     * - 성공 시 바로 결과 반환
     */
    private List<Article> tryCollect(CollectorService c, Source s, String kw) {
        int attempt = 0;
        while (true) {
            attempt++;
            long start = System.currentTimeMillis();
            try {
                // null 을 빈 리스트로 치환해서 이후 처리 단순화
                List<Article> items = Optional.ofNullable(c.collect(s, kw)).orElseGet(List::of);
                long took = System.currentTimeMillis() - start;
                log.debug("[crawl] collected type={} srcCode={} kw='{}' count={} in {}ms (attempt={})",
                        s.getCollector(), s.getCode(), kw, items.size(), took, attempt);
                return items;
            } catch (Exception e) {
                // 마지막 시도까지 실패하면 예외 전파 → exceptionally()에서 로그로 처리
                if (attempt > Math.max(0, RETRIES)) throw e;
                // 백오프 후 재시도 (인터럽트되면 즉시 종료)
                sleepQuiet(RETRY_BACKOFF_MS);
            }
        }
    }

    /** 스레드 sleep 유틸 (인터럽트 시 현재 스레드 상태 복구) */
    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
