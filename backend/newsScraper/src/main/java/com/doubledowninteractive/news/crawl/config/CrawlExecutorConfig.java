package com.doubledowninteractive.news.crawl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class CrawlExecutorConfig {

    @Bean(name = "crawlExecutor")
    public Executor crawlExecutor() {
        // 🔢 필요하면 숫자만 바꾸면 됩니다
        int maxConcurrency = 6;
        int queue = 100;

        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(maxConcurrency);
        ex.setMaxPoolSize(maxConcurrency);
        ex.setQueueCapacity(queue);
        ex.setThreadNamePrefix("crawl-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}
