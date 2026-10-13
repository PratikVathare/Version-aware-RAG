package com.enterprise.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${rag.embedding.service-url:http://localhost:8000/embed}")
    private String embeddingServiceUrl;

    @Bean
    public RestClient embeddingRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60)); // Model inference may take a few seconds on first run

        return RestClient.builder()
                .baseUrl(embeddingServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
