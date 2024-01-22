package com.medilabosolutions.configuration.route;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.medilabosolutions.configuration.filter.AuthorizationHeaderFilter;
import com.medilabosolutions.configuration.filter.AuthorizationHeaderFilter.Config;

@Configuration
public class GlatewayRoutesConfig {

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, AuthorizationHeaderFilter authorizationHeaderFilter) {


                return builder.routes()
                                // route for patient-service
                                .route("patient-service", r -> r.path("/api/v1/patients/**")
                                                .filters(f -> f.stripPrefix(2)
                                                                .filter(authorizationHeaderFilter.apply(new Config()), 1))
                                                .uri("lb://PATIENT-SERVICE"))

                                // route for note-service
                                .route("note-service", r -> r.path("/api/v1/notes/**")
                                                .filters(f -> f.stripPrefix(2)
                                                                .filter(authorizationHeaderFilter.apply(new Config()), 1))
                                                .uri("lb://NOTE-SERVICE"))

                                // route for risk-service                
                                .route("risk-service", r -> r.path("/api/v1/risks/**")
                                                .filters(f -> f.stripPrefix(2)
                                                                .filter(authorizationHeaderFilter.apply(new Config()), 1))
                                                .uri("lb://RISK-SERVICE"))
                                                
                                // route for auth-service
                                .route("authentication", r -> r.method("POST")
                                                .and()
                                                .path("/login")
                                                .uri("lb://AUTH-SERVER"))

                                .build();
        }


}
