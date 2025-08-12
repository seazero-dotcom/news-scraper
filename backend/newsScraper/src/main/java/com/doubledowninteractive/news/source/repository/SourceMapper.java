package com.doubledowninteractive.news.source.repository;

import com.doubledowninteractive.news.source.domain.Source;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SourceMapper {
    // 목록/단건
    List<Source> findAll();
    Source findById(@Param("id") Long id);

    // enabled=1 조건까지 포함된 조회 (스케줄러에서 사용)
    Source findByCode(@Param("code") String code);

    // 🔹 추가: 스케줄러용
    List<Source> findAllEnabled();

    // CUD
    int insert(@Param("code") String code,
               @Param("name") String name,
               @Param("baseUrl") String baseUrl,
               @Param("collector") String collector,
               @Param("params") String paramsJson);

    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    int update(@Param("id") Long id,
               @Param("name") String name,
               @Param("baseUrl") String baseUrl,
               @Param("collector") String collector,
               @Param("params") String paramsJson);

    int deleteById(@Param("id") Long id);
}
