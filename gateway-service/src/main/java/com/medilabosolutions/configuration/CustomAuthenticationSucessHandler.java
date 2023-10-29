package com.medilabosolutions.configuration;

import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class CustomAuthenticationSucessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        // TODO create JwToken and add to exchange headers

        ServerWebExchange exchange = webFilterExchange.getExchange();
        exchange.getResponse().getHeaders().add(org.springframework.http.HttpHeaders.AUTHORIZATION, "test_authentication");
// log.info("################ location de la requete :{}",exchange.getRequest().get);
        URI redirectUri = java.net.URI.create("/");
        
        return new WebSessionServerRequestCache().getRedirectUri(exchange)
                .defaultIfEmpty(redirectUri)
                .flatMap((location) -> new DefaultServerRedirectStrategy().sendRedirect(exchange, redirectUri));
    }

}
