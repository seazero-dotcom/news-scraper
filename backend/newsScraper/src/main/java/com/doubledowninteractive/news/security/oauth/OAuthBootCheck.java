package com.doubledowninteractive.news.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class OAuthBootCheck {
    OAuthBootCheck(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String cid,
            @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String csec,
            @Value("${spring.security.oauth2.client.registration.google.redirect-uri:}") String ru
    ) {
        System.out.println("OAUTH google.client-id   = " + (cid.isBlank() ? "(EMPTY)" : cid));
        System.out.println("OAUTH google.client-secret= " + (csec.isBlank() ? "(EMPTY)" : "(SET) length=" + csec.length()));
        System.out.println("redirect-uri = " + ru);
    }
}

