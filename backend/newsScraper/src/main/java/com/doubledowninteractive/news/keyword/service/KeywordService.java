package com.doubledowninteractive.news.keyword.service;

import com.doubledowninteractive.news.common.exception.DuplicateException;
import com.doubledowninteractive.news.keyword.domain.Keyword;
import com.doubledowninteractive.news.keyword.repository.KeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class KeywordService {
    private final KeywordMapper mapper;

    public List<Keyword> findAll() { return mapper.findAll(); }
    public List<Keyword> findAllEnabled() { return mapper.findAllEnabled(); }

    @Transactional
    public void add(String word) {
        String normalized = word == null ? "" : word.trim();
        if (normalized.isEmpty()) {
            // 빈 문자열에 대한 처리: 여기선 무시하거나, BadRequestException을 던져도 됨
            throw new IllegalArgumentException("키워드가 비어 있습니다.");
        }

        // 1) 사전 확인 (UX용): 이미 있다면 바로 안내
        if (mapper.findByWord(normalized) != null) {
            throw new DuplicateException("이미 등록된 키워드입니다: " + normalized);
        }

        // 2) 경쟁 조건 대비: 실제 INSERT 시 중복키 예외를 잡아 안내
        try {
            mapper.insert(normalized);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException("이미 등록된 키워드입니다: " + normalized);
        }
    }

    @Transactional
    public void toggle(Long id, boolean enabled) {
        mapper.updateEnabled(id, enabled);
    }

    @Transactional
    public void remove(Long id) {
        mapper.deleteById(id);
    }
}
