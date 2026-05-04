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
public class IngestionServiceRoutes {

    @Value("${services.ingestion.url}")
    private String ingestionServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> ingestionServiceRoute() {
        return route("ingestion-service-route")
                .route(RequestPredicates.path("/api/v1/ingestions/**"), http())
                .before(uri(ingestionServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "ingestionServiceCircuitBreaker", URI.create("forward:/fallback/ingestion-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> ingestionServiceFallbackRoute() {
        return route("ingestion-service-fallback")
                .route(RequestPredicates.path("/fallback/ingestion-service"), _ -> ServerResponse.status(
                                HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Ingestion Service is not available"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> ingestionServiceApiDocs() {
        return GatewayRouterFunctions.route("ingestion-service-api-docs")
                .route(RequestPredicates.path("/docs/ingestion-service"), http())
                .before(uri(ingestionServiceUrl + "/v3/api-docs"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
