package com.doubledowninteractive.news.security;
import com.doubledowninteractive.news.security.jwt.JwtUser;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtils {
    private AuthUtils(){}
    public static Long currentUserId() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        if (a.getPrincipal() instanceof JwtUser u) return u.getId();
        return null;
    }
}
