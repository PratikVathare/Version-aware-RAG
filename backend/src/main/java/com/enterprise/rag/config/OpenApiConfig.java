package com.enterprise.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI versionAwareRagOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Version-Aware RAG API")
                        .description("Enterprise Retrieval Augmented Generation API with Temporal and Version-Aware Document Partitioning")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enterprise Engineering Team"))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}
