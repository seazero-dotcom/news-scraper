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
