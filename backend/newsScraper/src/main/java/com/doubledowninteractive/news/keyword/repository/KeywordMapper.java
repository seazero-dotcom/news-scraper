package com.doubledowninteractive.news.keyword.repository;

import com.doubledowninteractive.news.keyword.domain.Keyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KeywordMapper {
    List<Keyword> findAll(@Param("userId") Long userId);
    List<Keyword> findAllEnabled(@Param("userId") Long userId);

    // 🔹 사전 중복 확인용
    Keyword findByWord(@Param("userId") Long userId, @Param("word") String word);

    // 🔹 일반 INSERT (IGNORE 아님)
    int insert(@Param("userId") Long userId, @Param("word") String word);

    int updateEnabled(@Param("userId") Long userId, @Param("id") Long id, @Param("enabled") boolean enabled);
    int deleteById(@Param("userId") Long userId, @Param("id") Long id);
}
