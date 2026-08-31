package com.case_study.identity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI metadata configuration.
 * Available at /swagger-ui.html once the application is running.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI identityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Asynchronous User Notification System API")
                        .description("Case study: decoupled user registration with JMS-based async notification processing")
                        .version("1.0.0")
                        .contact(new Contact().name("jomanah").email("Jomanahmf@gmail.com")));
    }
}
