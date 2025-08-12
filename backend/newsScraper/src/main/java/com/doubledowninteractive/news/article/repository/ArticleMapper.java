package com.doubledowninteractive.news.article.repository;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ArticleMapper {
    int insertIgnore(Article a);

    long count(Long userId,
            @Param("q") String q,
               @Param("sourceId") Long sourceId,
               @Param("keywordId") Long keywordId,
               @Param("from") LocalDate from,
               @Param("to") LocalDate to);

    List<ArticleListItemDto> findList(Long userId,
            @Param("q") String q,
                                      @Param("sourceId") Long sourceId,
                                      @Param("keywordId") Long keywordId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      @Param("sort") String sort,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    int countBySourceId(@Param("sourceId") Long sourceId);
    int deleteBySourceId(@Param("sourceId") Long sourceId);
}

