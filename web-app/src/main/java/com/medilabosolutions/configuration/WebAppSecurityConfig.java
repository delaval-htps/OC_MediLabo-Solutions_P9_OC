package com.medilabosolutions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;


@Configuration
@EnableWebFluxSecurity
public class WebAppSecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http.authorizeExchange(exchanges -> exchanges.pathMatchers(HttpMethod.POST, "/login").permitAll().anyExchange().permitAll())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .csrf(Customizer.withDefaults())
                .securityContextRepository(new WebSessionServerSecurityContextRepository());

        return http.build();
    }

}
