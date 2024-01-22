package com.medilabosolutions.configuration.filter;

import java.util.Set;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalPrefilterLogResquest implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("\t ********PREFILTER IS EXECUTED************");
        String requestPath = exchange.getRequest().getPath().toString();
        log.info("\t * requestPath is {}", requestPath);

        HttpHeaders headers = exchange.getRequest().getHeaders();
        Set<String> headerNames = headers.keySet();

        headerNames.forEach(headerName -> {
            String headerValue = headers.getFirst(headerName);
            log.info("\t * {} : {}", headerName, headerValue);
        });

        log.info("\t ********PREFILTER IS FINISHED************\n");
        return chain.filter(exchange);

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
