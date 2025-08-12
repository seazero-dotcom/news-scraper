package com.doubledowninteractive.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NewsScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsScraperApplication.class, args);
    }

}
