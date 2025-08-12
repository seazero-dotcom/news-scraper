package com.doubledowninteractive.news.security.jwt;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class JwtUser extends User {
    private final Long id;
    public JwtUser(Long id, String email, Collection<? extends GrantedAuthority> auths){
        super(email, "", auths); this.id = id;
    }
    public Long getId(){ return id; }
}
