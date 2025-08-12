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

    private final SourceMapper mapper;
    private final ArticleMapper articleMapper;

    private final ObjectMapper om = new ObjectMapper();

    /* ===================== 조회 ===================== */

    public List<Source> findAll() {
        return mapper.findAll();
    }

    public List<Source> findAllEnabled() {
        return mapper.findAllEnabled();
    }

    /** enabled=1 조건을 포함하는 code 조회 (스케줄러 등에서 사용) */
    public Source findEnabledByCode(String code) {
        return mapper.findByCode(code);
    }

    public Source getOrThrow(Long id) {
        Source s = mapper.findById(id);
        if (s == null) throw new NotFoundException("존재하지 않는 소스: id=" + id);
        return s;
    }

    /* ===================== 생성 ===================== */

    /**
     * (이전 호환) 세 파라미터 버전은 더 이상 지원하지 않음.
     * site 정보가 없으면 수집이 불가하므로 명시적으로 막는다.
     */
    @Transactional
    public void add(String code, String name, String baseUrl) {
        throw new IllegalArgumentException("AGGREGATOR_RSS_SITE만 지원합니다. params.site가 필요한 add(...) 오버로드를 사용하세요.");
    }

    /** collector/params까지 받는 정식 생성 — collector는 강제로 AGGREGATOR_RSS_SITE */
    @Transactional
    public void add(String code, String name, String baseUrl,
                    String collector, Map<String, Object> params) {
        String c = (code == null ? "" : code.trim().toUpperCase());
        String n = (name == null ? "" : name.trim());
        String b = (baseUrl == null ? "" : baseUrl.trim());

        if (c.isEmpty() || n.isEmpty()) {
            throw new IllegalArgumentException("code/name은 비워둘 수 없습니다.");
        }
        // collector 강제
        String col = ONLY_COLLECTOR;

        // site 필수 검증
        validateParams(col, params);

        String paramsJson = null;
        try {
            if (params != null) paramsJson = om.writeValueAsString(params);
        } catch (Exception ignore) {}

        try {
            // Mapper는 (code, name, baseUrl, collector, paramsJson) 순서로 받음
            mapper.insert(c, n, b, col, paramsJson);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException("이미 존재하는 코드입니다: " + c);
        }
    }

    /* ===================== 수정 ===================== */

    /**
     * name/baseUrl/enabled/collector/params 부분 수정 (null인 값은 변경 안 함)
     * collector는 AGGREGATOR_RSS_SITE만 허용, params.site 필수
     */
    @Transactional
    public void update(Long id, String name, String baseUrl, Boolean enabled,
                       String collector, Map<String, Object> params) {
        // 존재 확인
        Source s = getOrThrow(id);

        // collector는 강제로 AGGREGATOR_RSS_SITE만
        String newCollector = ONLY_COLLECTOR;

        // params가 왔다면 site 필수 검증
        if (params != null) {
            validateParams(newCollector, params);
        }

        String paramsJson = null;
        try {
            if (params != null) paramsJson = om.writeValueAsString(params);
        } catch (Exception ignore) {}

        // 부분 업데이트
        mapper.update(id, name, baseUrl, newCollector, paramsJson);

        // enabled가 함께 왔다면 별도 처리
        if (enabled != null) {
            int updated = mapper.updateEnabled(id, enabled);
            if (updated == 0) throw new NotFoundException("존재하지 않는 소스: id=" + id);
        }
    }

    /* ===================== 토글 ===================== */

    @Transactional
    public void toggle(Long id, boolean enabled) {
        int updated = mapper.updateEnabled(id, enabled);
        if (updated == 0) throw new NotFoundException("존재하지 않는 소스: id=" + id);
    }

    /* ===================== 삭제 ===================== */

    /**
     * force=false: 연관 기사 있으면 409 (권장)
     * force=true : 연관 기사 먼저 삭제 후 소스 삭제
     */
    @Transactional
    public void remove(Long id, boolean force) {
        Source s = mapper.findById(id);
        if (s == null) throw new NotFoundException("존재하지 않는 소스: id=" + id);

        int refCount = articleMapper.countBySourceId(id);
        if (refCount > 0 && !force) {
            throw new ResourceInUseException(refCount + "건의 연관 기사 때문에 삭제할 수 없습니다.");
        }

        if (refCount > 0 && force) {
            articleMapper.deleteBySourceId(id);
        }

        int deleted = mapper.deleteById(id);
        if (deleted == 0) throw new NotFoundException("삭제 실패: id=" + id);
    }

    /* ===================== 내부 유틸 ===================== */

    private void validateParams(String collector, Map<String, Object> params) {
        // collector는 이미 강제로 ONLY_COLLECTOR
        if (params == null || isBlank((String) params.get("site"))) {
            throw new IllegalArgumentException("AGGREGATOR_RSS_SITE는 params.site 가 필요합니다");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
