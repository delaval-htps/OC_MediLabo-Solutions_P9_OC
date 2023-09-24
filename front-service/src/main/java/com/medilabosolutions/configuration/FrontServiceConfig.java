package com.medilabosolutions.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FrontServiceConfig {

    @Bean
    @LoadBalanced
    public WebClient loadBalancerWebClientBuilder() {
        return WebClient.create();
    }

    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }
}
