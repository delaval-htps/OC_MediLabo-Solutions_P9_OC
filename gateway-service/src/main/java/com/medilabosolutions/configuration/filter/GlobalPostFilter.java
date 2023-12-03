package com.medilabosolutions.configuration.filter;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
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

            // ServerHttpResponseDecorator responseDecorator = getDecoratedResponse(exchange.getResponse(), exchange.getRequest(), exchange.getResponse().bufferFactory());
           

            log.info("\t ********POSTFILTER IS FINISHED************\n");

        }));
    }

    @Override
    public int getOrder() {
        return 12;
    }

    private ServerHttpResponseDecorator getDecoratedResponse(ServerHttpResponse response, ServerHttpRequest request, DataBufferFactory dataBufferFactory) {

        return new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {

                if (body instanceof Flux) {

                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {

                        DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
                        byte[] content = new byte[joinedBuffers.readableByteCount()];
                        joinedBuffers.read(content);
                        String responseBody = new String(content, StandardCharsets.UTF_8);// MODIFY RESPONSE and Return the Modified response
                        log.info("response body :{}", responseBody);

                        return dataBufferFactory.wrap(responseBody.getBytes());
                    })).onErrorResume(err -> {

                        log.error("error while decorating Response: {}", err.getMessage());
                        return Mono.empty();
                    });

                }
                return super.writeWith(body);
            }
        };
    }
}
