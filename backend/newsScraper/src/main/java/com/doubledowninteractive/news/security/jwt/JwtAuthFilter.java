package com.doubledowninteractive.news.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;

    public JwtAuthFilter(JwtTokenProvider jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // Authorization: Bearer <token>
        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // 이미 누가 인증 넣어놨다면 덮어쓰지 않음 (중복 작업 방지)
                Authentication existing = SecurityContextHolder.getContext().getAuthentication();
                if (existing == null && jwt.validate(token)) {
                    Authentication auth = jwt.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // 토큰 파싱/검증 실패 시 컨텍스트를 비워 안전하게 진행
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(req, res);
    }
}
