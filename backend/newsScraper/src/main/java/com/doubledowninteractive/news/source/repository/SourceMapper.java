package com.doubledowninteractive.news.source.repository;

import com.doubledowninteractive.news.source.domain.Source;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SourceMapper {
    List<Source> findAll(@Param("userId") Long userId);
    Source findById(@Param("userId") Long userId, @Param("id") Long id);


    Source findByCode(@Param("code") String code);


    List<Source> findAllEnabled(@Param("userId") Long userId);


    int insert(@Param("userId") Long userId,
               @Param("code") String code,
               @Param("name") String name,
               @Param("baseUrl") String baseUrl,
               @Param("collector") String collector,
               @Param("params") String paramsJson,
               @Param("enabled") boolean enabled);

    int updateEnabled(@Param("userId") Long userId, @Param("id") Long id, @Param("enabled") boolean enabled);

    int update(@Param("userId") Long userId,
               @Param("id") Long id,
               @Param("name") String name,
               @Param("baseUrl") String baseUrl,
               @Param("collector") String collector,
               @Param("params") String paramsJson);

    int deleteById(@Param("userId") Long userId, @Param("id") Long id);
}
