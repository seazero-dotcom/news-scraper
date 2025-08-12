// backend/src/main/java/com/doubledowninteractive/news/security/SecurityConfig.java
package com.doubledowninteractive.news.security;

import com.doubledowninteractive.news.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.doubledowninteractive.news.security.jwt.JwtAuthFilter jwtAuthFilter;

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    /* ===================== 1) API 체인: /api/**  ===================== */
    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()      // CORS preflight
                        .requestMatchers("/api/articles/**").permitAll()             // 공개 목록/조회는 그대로
                        .anyRequest().authenticated()
                )
                // 미인증 시 401 반환(브라우저가 구글로 302 리다이렉트하지 않게)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // JWT 인증 필터 (여기서만 동작하면 충분)
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    /* ===================== 2) WEB 체인: OAuth2 로그인 ===================== */
    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/favicon.ico", "/default-ui.css",
                                "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(o -> o
                        // 별도 핸들러 파일 없이, 성공 시 프론트로 토큰 전달 리다이렉트
                        .successHandler((req, res, authentication) -> {
                            String subject = extractSubject(authentication);
                            String name    = extractName(authentication);
                            String email   = extractEmail(authentication);
                            Collection<String> roles = extractRoleNames(authentication);

                            // JwtTokenProvider 시그니처에 맞춰 발급
                            String token = jwtTokenProvider.createToken(subject, name, email, roles);

                            String redirect = req.getParameter("redirect");
                            if (redirect == null || redirect.isBlank()) redirect = "/settings";

                            String url = frontendBaseUrl + "/oauth2/success"
                                    + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                                    + "&redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
                            res.sendRedirect(url);
                        })
                );

        return http.build();
    }

    /* ===================== CORS ===================== */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("http://localhost:5173","http://127.0.0.1:5173"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH")); // ✅ PATCH 추가
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }


    /* ===================== 내부 유틸/필터 ===================== */


    private String extractSubject(Authentication authentication) {
        // 선호 순서: email > sub > name > principal.toString()
        String email = extractEmail(authentication);
        if (email != null && !email.isBlank()) return email;

        Object p = authentication.getPrincipal();
        if (p instanceof OidcUser ou) {
            String sub = ou.getSubject();
            if (sub != null && !sub.isBlank()) return sub;
        }
        if (p instanceof OAuth2User o) {
            Object sub = o.getAttributes().get("sub");
            if (sub instanceof String s && !s.isBlank()) return s;
        }
        String name = extractName(authentication);
        if (name != null && !name.isBlank()) return name;

        return String.valueOf(p);
    }

    private String extractName(Authentication authentication) {
        Object p = authentication.getPrincipal();
        if (p instanceof OidcUser ou) {
            String name = ou.getFullName();
            if (name == null || name.isBlank()) name = ou.getGivenName();
            if (name != null && !name.isBlank()) return name;
        }
        if (p instanceof OAuth2User o) {
            Object name = o.getAttributes().get("name");
            if (name instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private String extractEmail(Authentication authentication) {
        Object p = authentication.getPrincipal();
        if (p instanceof OidcUser ou) {
            String email = ou.getEmail();
            if (email != null && !email.isBlank()) return email;
        }
        if (p instanceof OAuth2User o) {
            Object email = o.getAttributes().get("email");
            if (email instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private Collection<String> extractRoleNames(Authentication authentication) {
        Collection<? extends GrantedAuthority> auths = authentication.getAuthorities();
        List<String> roles = new ArrayList<>();
        if (auths != null) {
            for (GrantedAuthority ga : auths) {
                roles.add(ga.getAuthority());
            }
        }
        if (roles.isEmpty()) roles.add("ROLE_USER");
        return roles;
    }
}
