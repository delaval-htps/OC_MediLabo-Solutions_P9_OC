package com.medilabosolutions.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;


@Configuration
@EnableWebFluxSecurity
public class FrontSecurityConfig {

    @Autowired
    private CsrfHeaderFilter csrfHeaderFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                .addFilterBefore(csrfHeaderFilter, SecurityWebFiltersOrder.CSRF)
                .authorizeExchange(exchanges -> exchanges
                        // .pathMatchers( "/public/**", "/favicon.ico").permitAll()
                        .pathMatchers("/**").permitAll()
                        .anyExchange().permitAll())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                ;
              
        return http.build();
    }

}
