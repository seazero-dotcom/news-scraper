package com.doubledowninteractive.news.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expMinutes:60}") private long expMinutes;

    private byte[] key() { return secret.getBytes(StandardCharsets.UTF_8); }

    public String createToken(String subject, String name, String email, Collection<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMinutes * 60_000);
        return Jwts.builder()
                .setSubject(subject)
                .claim("name", name)
                .claim("email", email)
                .claim("roles", roles == null ? List.of("ROLE_USER") : roles)
                .setIssuedAt(now).setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(key()), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(key())).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public Authentication getAuthentication(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(key())).build()
                .parseClaimsJws(token).getBody();
        String email = c.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) c.getOrDefault("roles", List.of("ROLE_USER"));
        List<GrantedAuthority> auths = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        User principal = new User(email == null ? "user" : email, "", auths);
        return new UsernamePasswordAuthenticationToken(principal, token, auths);
    }
}
