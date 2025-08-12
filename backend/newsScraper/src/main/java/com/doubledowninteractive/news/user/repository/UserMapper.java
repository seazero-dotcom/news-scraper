package com.doubledowninteractive.news.user.repository;
import com.doubledowninteractive.news.user.domain.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    User findByEmail(@Param("email") String email);
    int  insert(@Param("email") String email, @Param("name") String name);
    List<Long> findAllIds();

    Long findById(@Param("email") String email);

    Long findIdByEmail(String email);
}
