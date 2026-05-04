package com.enerlytics.gateways.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class UserServiceRoutes {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service-route")
                .route(RequestPredicates.path("/api/v1/users/**"), http())
                .before(uri(userServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "userServiceCircuitBreaker", URI.create("forward:/fallback/user-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceFallbackRoute() {
        return route("user-service-fallback")
                .route(RequestPredicates.path("/fallback/user-service"), _ -> ServerResponse.status(
                                HttpStatus.SERVICE_UNAVAILABLE)
                        .body("User Service is not available"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceApiDocs() {
        return GatewayRouterFunctions.route("user-service-api-docs")
                .route(RequestPredicates.path("/docs/user-service"), http())
                .before(uri(userServiceUrl + "/v3/api-docs"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
