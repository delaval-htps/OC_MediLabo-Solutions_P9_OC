package com.medilabosolutions.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
                // TODO BIG ONE : redefine routes with api/V1/patients for patient-service & / for
                // /front
                return builder.routes()
                                // route for patient-service

                                .route("patient-service",
                                                r -> r.path("/patients/**")
                                                                .uri("lb://PATIENT-SERVICE"))
                                /*
                                 * no need to change path because patient-service has a
                                 * requestMapping("/patients")
                                 */

                                // route for front-service

                                .route("front-service",
                                                r -> r.path("/front/**")
                                                                /*
                                                                 * need to delete "front" from path
                                                                 * because front-service has a
                                                                 * requestMapping("/")
                                                                 */
                                                                .filters(f -> f.stripPrefix(1))
                                                                .uri("lb://FRONT-SERVICE"))
                                .build();
        }
}
