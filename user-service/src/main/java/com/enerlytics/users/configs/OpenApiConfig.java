package com.enerlytics.users.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enerlytics User Service API")
                        .description("Manages users and alerting preferences for Enerlytics.")
                        .version("v1")
                        .contact(new Contact().name("Enerlytics Team").email("api@enerlytics.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local environment")));
    }
}
