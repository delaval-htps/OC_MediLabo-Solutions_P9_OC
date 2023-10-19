package com.medilabosolutions.configuration;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CsrfHeaderFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Mono<CsrfToken> token = (Mono<CsrfToken>) exchange.getAttributes().get(CsrfToken.class.getName());
        if (token != null){
            log.debug("CSRFTOKEN #####################"); 
        return token.flatMap(t->chain.filter(exchange));
        }
        return chain.filter(exchange);
    }
}
