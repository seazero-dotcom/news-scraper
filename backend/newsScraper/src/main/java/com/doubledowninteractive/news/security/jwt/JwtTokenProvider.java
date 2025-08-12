package com.doubledowninteractive.news.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expMinutes:60}") private long expMinutes;

    private byte[] key() { return secret.getBytes(StandardCharsets.UTF_8); }

    /**
     * 구글 로그인 성공 시, 사용자 정보(subject/name/email/roles)로 JWT를 만든다
     * SecurityConfig의 successHandler에서 이 메서드를 호출해서 토큰을 만들고,
     * 프론트로 리다이렉트할 때 ?token=...으로 넘긴다
     */
    public String createToken(String subject, String name, String email, Collection<String> roles, Long uid) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMinutes * 60_000);
        return Jwts.builder()
                .setSubject(subject)
                .claim("uid", uid)
                .claim("name", name)
                .claim("email", email)
                .claim("roles", roles == null ? List.of("ROLE_USER") : roles)
                .setIssuedAt(now).setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(key()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * API 호출 때 헤더의 Authorization: Bearer ... 토큰이 변조/만료되지 않았는지 검증
     * JwtAuthFilter에서 이걸 호출하고, 통과하면 아래로 넘어간다
     */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(key())).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) { return false; }
    }

    /**
     * 토큰 속 사용자 정보(email/roles 등)를 꺼내 Spring Security의 Authentication 객체로 바꾼다
     * 만들어진 인증 객체를 SecurityContext에 넣어 /api/** 접근을 인증된 사용자로 처리
     */
    public Authentication getAuthentication(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(key())).build()
                .parseClaimsJws(token).getBody();
        Long uid = c.get("uid", Long.class).longValue();
        String email = c.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) c.getOrDefault("roles", List.of("ROLE_USER"));
        List<GrantedAuthority> auths = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        JwtUser principal = new JwtUser(uid, email == null ? "user" : email, auths); // email이 없으면 user로 설정
        return new UsernamePasswordAuthenticationToken(principal, token, auths);
    }
}
