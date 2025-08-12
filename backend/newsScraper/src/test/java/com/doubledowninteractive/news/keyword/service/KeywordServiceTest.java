package com.doubledowninteractive.news.keyword.service;

import com.doubledowninteractive.news.crawl.scheduler.CrawlScheduler;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.repository.KeywordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock KeywordMapper mapper;
    @Mock CrawlScheduler crawlScheduler;

    @InjectMocks KeywordService service;

    @AfterEach
    void cleanupTxSync() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void add_firstTime_callsScheduler_afterCommit() {
        // GIVEN
        String word = "부동산";
        Long userId = 12213L;
        Keyword created = new Keyword();
        created.setId(userId);
        created.setWord(word);

        // mapper.findByWord(userId, word) 가 처음 호출되면 → null을 반환
        // 같은 메서드가 두 번째 호출되면 → created 반환
        when(mapper.findByWord(userId, word)).thenReturn(null, created);

        // 트랜잭션 동기화 수동
        TransactionSynchronizationManager.initSynchronization();

        // WHEN
        // 서비스의 add 메서드를 호출하여 키워드를 추가
        service.add(userId, word);


        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            // 트랜잭션 커밋 후에 runOnceForNewKeyword 호출
            sync.afterCommit();
        }

        // Captor
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> createdIdCaptor = ArgumentCaptor.forClass(Long.class);

        // mapper.insert 호출 캡처
        verify(mapper).insert(idCaptor.capture(), wordCaptor.capture());
        System.out.println("=== mapper.insert 호출 인자 ===");
        System.out.println("id: " + idCaptor.getValue());
        System.out.println("word: " + wordCaptor.getValue());

        // 추가된 키워드가 올바르게 삽입되었는지 확인
        verify(crawlScheduler).runOnceForNewKeyword(
                idCaptor.capture(),
                wordCaptor.capture(),
                createdIdCaptor.capture()
        );
        System.out.println("=== crawlScheduler.runOnceForNewKeyword 호출 인자 ===");
        System.out.println("id: " + idCaptor.getValue());
        System.out.println("word: " + wordCaptor.getValue());
        System.out.println("createdId: " + createdIdCaptor.getValue());
    }
}
