package com.medilabosolutions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class FrontSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers( "/public/**", "/favicon.ico").permitAll()
                // .pathMatchers("/**").permitAll())
                .anyExchange().permitAll())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .csrf(CsrfSpec::disable);
        return http.build();
    }


}
