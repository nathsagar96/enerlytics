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
public class InsightServiceRoutes {

    @Value("${services.insight.url}")
    private String insightServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> insightServiceRoute() {
        return route("insight-service-route")
                .route(RequestPredicates.path("/api/v1/insights/**"), http())
                .before(uri(insightServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "insightServiceCircuitBreaker", URI.create("forward:/fallback/insight-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> insightServiceFallbackRoute() {
        return route("insight-service-fallback")
                .route(RequestPredicates.path("/fallback/insight-service"), _ -> ServerResponse.status(
                                HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Insight Service is not available"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> insightServiceApiDocs() {
        return GatewayRouterFunctions.route("insight-service-api-docs")
                .route(RequestPredicates.path("/docs/insight-service"), http())
                .before(uri(insightServiceUrl + "/v3/api-docs"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
