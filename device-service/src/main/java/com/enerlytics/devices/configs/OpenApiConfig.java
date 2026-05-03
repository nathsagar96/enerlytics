package com.enerlytics.devices.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.server-url}")
    private String serverUrl;

    @Value("${app.openapi.server-description}")
    private String serverDescription;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enerlytics Device Service API")
                        .description("Manages user devices and metadata in Enerlytics.")
                        .version("v1")
                        .contact(new Contact().name("Enerlytics Team").email("api@enerlytics.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url(serverUrl).description(serverDescription)));
    }
}
