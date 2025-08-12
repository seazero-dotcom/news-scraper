package com.doubledowninteractive.news.keyword.service;

import com.doubledowninteractive.news.common.exception.DuplicateException;
import com.doubledowninteractive.news.crawl.scheduler.CrawlScheduler;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.repository.KeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordMapper mapper;
    private final CrawlScheduler crawlScheduler;

    public List<Keyword> findAllByUser(Long userId) {
        return mapper.findAll(userId);
    }
    public List<Keyword> findAllEnabledByUser(Long userId) {
        return mapper.findAllEnabled(userId);
    }

    @Transactional
    public void add(Long userId, String word) {
        String w = word == null ? "" : word.trim();
        if (w.isEmpty()) throw new IllegalArgumentException("키워드를 입력해 주세요.");

        if (mapper.findByWord(userId, w) != null) {
            throw new DuplicateException("이미 등록된 키워드입니다.");
        }

        mapper.insert(userId, w);

        Keyword k = mapper.findByWord(userId, w);
        if (k == null) return;

        // 트랜잭션 커밋 후 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                crawlScheduler.runOnceForNewKeyword(userId, k.getWord(), k.getId());
            }
        });
    }

    @Transactional
    public void toggle(Long userId, Long id, boolean enabled) {
        mapper.updateEnabled(userId, id, enabled);
    }

    @Transactional
    public void remove(Long userId, Long id) {
        mapper.deleteById(userId, id);
    }
}
