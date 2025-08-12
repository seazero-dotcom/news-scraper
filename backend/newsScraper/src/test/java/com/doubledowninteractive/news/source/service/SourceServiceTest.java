package com.doubledowninteractive.news.source.service;

import com.doubledowninteractive.news.article.repository.ArticleMapper;
import com.doubledowninteractive.news.source.repository.SourceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SourceServiceTest {

    @Mock SourceMapper mapper;
    @Mock ArticleMapper articleMapper;

    @InjectMocks SourceService service;

    @Test
    void add_ok_withSite() {
        Map<String, Object> params = Map.of("site", "news.naver.com");

        // 실행
        service.add(123L, "NAVER","네이버 뉴스", "https://news.naver.com", true, params);

        // ArgumentCaptor 준비 (모든 파라미터 순서 맞추기)
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> siteCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> boolCaptor = ArgumentCaptor.forClass(Boolean.class);

        // verify + 값 캡처
        verify(mapper).insert(
                idCaptor.capture(),
                codeCaptor.capture(),
                nameCaptor.capture(),
                urlCaptor.capture(),
                typeCaptor.capture(),
                siteCaptor.capture(),
                boolCaptor.capture()
        );

        // 테스트 결과 로그 출력
        System.out.println("=== mapper.insert 호출 인자 ===");
        System.out.println("id: " + idCaptor.getValue());
        System.out.println("code: " + codeCaptor.getValue());
        System.out.println("name: " + nameCaptor.getValue());
        System.out.println("url: " + urlCaptor.getValue());
        System.out.println("type: " + typeCaptor.getValue());
        System.out.println("site: " + siteCaptor.getValue());
        System.out.println("flag: " + boolCaptor.getValue());

        // 검증 예시
        assertEquals("news.naver.com", siteCaptor.getValue());
    }
}
