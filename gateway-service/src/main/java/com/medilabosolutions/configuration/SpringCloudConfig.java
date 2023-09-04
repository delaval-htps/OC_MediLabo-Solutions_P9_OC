package com.medilabosolutions.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

                return builder.routes()
                                // route for patient-service
                                .route("patient-service",
                                                r -> r.path("/api/v1/patients/**")
                                                                /*
                                                                 * no need to change path because
                                                                 * patient-service has as path
                                                                 * "/patients": need to delete first
                                                                 * and second prefix "/api/v1"
                                                                 */
                                                                .filters(f -> f.stripPrefix(2))
                                                                .uri("lb://PATIENT-SERVICE"))
                                // route for front-service
                                .route("front-service",
                                                r -> r.path("/**")
                                                                .uri("lb://FRONT-SERVICE"))
                                .build();
        }
}
