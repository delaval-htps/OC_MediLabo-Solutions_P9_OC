package com.medilabosolutions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
public class CookieConfig {
    // TODO update config to have a secure jwt token
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("test");
        resolver.addCookieInitializer(builder -> builder.path("/"));
        resolver.addCookieInitializer(builder -> builder.httpOnly(true));
        return resolver;

    }
}
