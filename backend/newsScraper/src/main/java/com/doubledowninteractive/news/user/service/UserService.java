package com.doubledowninteractive.news.user.service;

import com.doubledowninteractive.news.user.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public Long findOrCreate(String email, String name) {
        // 이메일로 사용자 찾기 → 없으면 생성
        Long id = userMapper.findIdByEmail(email);
        if (id == null) {
            userMapper.insert(email, name);
            id = userMapper.findIdByEmail(email);
        }
        return id;
    }

    public List<Long> findAllIds () {
        return userMapper.findAllIds();
    }

    public Long currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        String email = authentication.getName();
        if (email.isEmpty()) {
            throw new IllegalStateException("인증 정보에 이메일이 없습니다.");
        }

        Long id = userMapper.findIdByEmail(email);
        return id;
    }

}
