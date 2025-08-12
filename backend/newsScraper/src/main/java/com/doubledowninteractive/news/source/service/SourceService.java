package com.doubledowninteractive.news.source.service;

import com.doubledowninteractive.news.article.repository.ArticleMapper;
import com.doubledowninteractive.news.common.exception.DuplicateException;
import com.doubledowninteractive.news.common.exception.NotFoundException;
import com.doubledowninteractive.news.common.exception.ResourceInUseException;
import com.doubledowninteractive.news.source.domain.Source;
import com.doubledowninteractive.news.source.repository.SourceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SourceService {

    private static final String ONLY_COLLECTOR = "AGGREGATOR_RSS_SITE";

    private final SourceMapper sourceMapper;
    private final ArticleMapper articleMapper;

    private final ObjectMapper om = new ObjectMapper();

    public List<Source> findAll(Long userId) {
        return sourceMapper.findAll(userId);
    }

    public List<Source> findAllEnabled(Long userId) {
        return sourceMapper.findAllEnabled(userId);
    }


    public Source getOrThrow(Long userId, Long id) {
        Source s = sourceMapper.findById(userId, id);
        if (s == null) throw new NotFoundException("존재하지 않는 소스: id=" + id);
        return s;
    }

    @Transactional
    public void add(Long userId, String code, String name, String baseUrl,
                    Boolean enabled, Map<String, Object> params) {
        String c = (code == null ? "" : code.trim().toUpperCase());
        String n = (name == null ? "" : name.trim());
        String b = (baseUrl == null ? "" : baseUrl.trim());
        boolean en = (enabled == null) || Boolean.TRUE.equals(enabled); // 기본 ON

        if (c.isEmpty() || n.isEmpty()) {
            throw new IllegalArgumentException("code/name은 비워둘 수 없습니다.");
        }

        String col = ONLY_COLLECTOR;

        validateParams(params);

        String paramsJson = null;
        try {
            if (params != null) paramsJson = om.writeValueAsString(params);
        } catch (Exception ignore) {}

        try {
            sourceMapper.insert(userId, c, n, b, col, paramsJson, en);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException("이미 존재하는 코드입니다: " + c);
        }
    }

    @Transactional
    public void update(Long userId, Long id, String name, String baseUrl, Boolean enabled,
                       String ignoredCollector, Map<String, Object> params) {

        getOrThrow(userId, id);

        String paramsJson = null;
        if (params != null) {
            validateParams(params);
            try { paramsJson = om.writeValueAsString(params); } catch (Exception ignore) {}
        }

        sourceMapper.update(userId, id, name, baseUrl, ONLY_COLLECTOR, paramsJson);

        if (enabled != null) {
            int updated = sourceMapper.updateEnabled(userId, id, enabled);
            if (updated == 0) throw new NotFoundException("존재하지 않는 소스: id=" + id);
        }
    }

    @Transactional
    public void toggle(Long userId, Long id, boolean enabled) {
        int updated = sourceMapper.updateEnabled(userId, id, enabled);
        if (updated == 0) throw new NotFoundException("존재하지 않는 소스: id=" + id);
    }

    @Transactional
    public void remove(Long userId, Long id, boolean force) {
        Source s = sourceMapper.findById(userId, id);
        if (s == null) throw new NotFoundException("존재하지 않는 소스: id=" + id);

        int refCount = articleMapper.countBySourceId(id);
        if (refCount > 0 && !force) {
            throw new ResourceInUseException(refCount + "건의 연관 기사 때문에 삭제할 수 없습니다.");
        }

        if (refCount > 0 && force) {
            articleMapper.deleteBySourceId(id);
        }

        int deleted = sourceMapper.deleteById(userId, id);
        if (deleted == 0) throw new NotFoundException("삭제 실패: id=" + id);
    }

    private void validateParams(Map<String, Object> params) {
        if (params == null) throw new IllegalArgumentException("params가 필요합니다");
        Object site = params.get("site");
        if (site == null || String.valueOf(site).trim().isEmpty()) {
            throw new IllegalArgumentException("AGGREGATOR_RSS_SITE는 params.site 가 필요합니다");
        }
    }
}
