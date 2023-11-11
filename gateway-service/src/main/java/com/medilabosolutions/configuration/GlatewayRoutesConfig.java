package com.medilabosolutions.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.medilabosolutions.predicate.AuthorizationHeaderFilter;
import com.medilabosolutions.predicate.AuthorizationHeaderFilter.Config;

@Configuration
public class GlatewayRoutesConfig {

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, AuthorizationHeaderFilter authorizationHeaderFilter) {


                return builder.routes()

                                // route for patient-service
                                .route("patient-service", r -> r.path("/api/v1/patients/**")
                                                // TODO voir si besoin de mettre le predicat ici car le authorizationheader fait déjà le boulot .header("jwtoken", "(.*)")

                                                // no need to change path because patient-service has as path "/patients": need to delete first and second prefix "/api/v1"
                                                .filters(f -> f.filter(authorizationHeaderFilter.apply(new Config()))
                                                                .stripPrefix(2))
                                                .uri("lb://PATIENT-SERVICE"))

                                // route for auth-service
                                .route("authentication", r -> r.path("/login")
                                                .uri("lb://AUTH-SERVER"))

                                .build();
        }


}
