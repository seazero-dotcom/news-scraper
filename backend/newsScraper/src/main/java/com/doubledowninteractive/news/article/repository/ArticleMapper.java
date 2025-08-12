package com.doubledowninteractive.news.article.repository;

import com.doubledowninteractive.news.article.domain.Article;
import com.doubledowninteractive.news.article.dto.ArticleListItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ArticleMapper {
    int insertIgnore(Article a);
    Article findById(@Param("id") Long id);

    long count(@Param("q") String q,
               @Param("sourceId") Long sourceId,
               @Param("keywordId") Long keywordId,
               @Param("from") LocalDate from,
               @Param("to") LocalDate to);

    List<ArticleListItemDto> findList(@Param("q") String q,
                                      @Param("sourceId") Long sourceId,
                                      @Param("keywordId") Long keywordId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      @Param("sort") String sort,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    List<Article> find(@Param("q") String q,
                       @Param("sourceId") Long sourceId,
                       @Param("keywordId") Long keywordId,
                       @Param("from") LocalDate from,
                       @Param("to") LocalDate to,
                       @Param("sort") String sort,
                       @Param("offset") int offset,
                       @Param("size") int size);


    // 🔹 추가
    int countBySourceId(@Param("sourceId") Long sourceId);
    int deleteBySourceId(@Param("sourceId") Long sourceId);
}

