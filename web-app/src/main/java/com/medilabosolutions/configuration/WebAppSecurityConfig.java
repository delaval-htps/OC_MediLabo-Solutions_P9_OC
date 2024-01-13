package com.medilabosolutions.configuration;

import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
public class WebAppSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.POST, "/login").permitAll()
                .anyExchange().permitAll())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .csrf(Customizer.withDefaults())
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .logout((logout) -> logout
                        .logoutHandler(customDelegatingServerLogoutHandler())
                        .logoutSuccessHandler(customLogoutSuccessHandler("/logout")));

        return http.build();
    }

    public DelegatingServerLogoutHandler customDelegatingServerLogoutHandler() {
        return new DelegatingServerLogoutHandler(
                new WebSessionServerLogoutHandler(), new SecurityContextServerLogoutHandler());
    }

    public ServerLogoutSuccessHandler customLogoutSuccessHandler(String logoutUrl) {
        RedirectServerLogoutSuccessHandler serverLogoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        serverLogoutSuccessHandler.setLogoutSuccessUrl(URI.create(logoutUrl));
        return serverLogoutSuccessHandler;
    }
}
