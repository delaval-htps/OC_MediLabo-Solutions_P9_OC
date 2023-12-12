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
public class GlobalPostFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {

            log.info("\t ********POSTFILTER IS EXECUTED************");
            String responseStatus = exchange.getResponse().getStatusCode().toString();
            log.info("\t * responseStatusCode is {}", responseStatus);

            HttpHeaders headers = exchange.getResponse().getHeaders();
            Set<String> headerNames = headers.keySet();

            headerNames.forEach(headerName -> {
                String headerValue = headers.getFirst(headerName);
                log.info("\t * {} : {}", headerName, headerValue);
            });

            log.info("\t ********POSTFILTER IS FINISHED************\n");

        }));
    }

    @Override
    public int getOrder() {
        return 12;
    }

}
