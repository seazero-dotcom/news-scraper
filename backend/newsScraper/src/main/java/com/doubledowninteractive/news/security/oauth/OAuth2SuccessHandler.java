package com.doubledowninteractive.news.security.oauth;

import com.doubledowninteractive.news.security.jwt.JwtTokenProvider;
import com.doubledowninteractive.news.user.service.UserService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final JwtTokenProvider jwt;
    private final UserService userService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontBase;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException {

        String email = null, name = null, sub = null;

        Object p = auth.getPrincipal();
        if (p instanceof DefaultOidcUser u) {
            Map<String, Object> a = u.getAttributes();
            email = u.getEmail();
            name = (String) a.getOrDefault("name", email);
            sub = u.getSubject();
        } else if (p instanceof DefaultOAuth2User u) {
            Map<String, Object> a = u.getAttributes();
            email = (String) a.getOrDefault("email", null);
            name = (String) a.getOrDefault("name", email);
            sub  = String.valueOf(a.getOrDefault("sub", email));
        }

        Long userId = null;
        if (email != null) {
            userId = userService.findOrCreate(email, name);
        }

        String token = jwt.createToken(sub != null ? sub : email, name, email, List.of("ROLE_USER"), userId);
        String redirect = frontBase + "/oauth2/success?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        res.sendRedirect(redirect);
    }
}
