package com.enerlytics.gateways.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class DeviceServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> deviceRoutes() {
        return route("device-service")
                .route(RequestPredicates.path("/api/v1/devices/**"), http())
                .before(uri("http://localhost:8081"))
                .build();
    }
}
