package com.medilabosolutions.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class GlobalFiltersConfiguration {
    @Bean
    @Order(1)
    public GlobalFilter secondFilter() {
        return (exchange, chain) -> {
            log.info("second global pre filter is executed.......");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("second global post filter is executed.......")));
        };

    }

    @Bean
    @Order(2)
    public GlobalFilter thirdFilter() {
        return (exchange, chain) -> {
            log.info("third global pre filter is executed.......");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("third second global post filter is executed.......")));
        };

    }
}
