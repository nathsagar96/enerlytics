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
public class DeviceServiceRoutes {

    @Value("${services.device.url}")
    private String deviceServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> deviceServiceRoute() {
        return route("device-service-route")
                .route(RequestPredicates.path("/api/v1/devices/**"), http())
                .before(uri(deviceServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "deviceServiceCircuitBreaker", URI.create("forward:/fallback/device-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceServiceFallbackRoute() {
        return route("device-service-fallback")
                .route(RequestPredicates.path("/fallback/device-service"), _ -> ServerResponse.status(
                                HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Device Service is not available"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceServiceApiDocs() {
        return GatewayRouterFunctions.route("device-service-api-docs")
                .route(RequestPredicates.path("/docs/device-service"), http())
                .before(uri(deviceServiceUrl + "/v3/api-docs"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
