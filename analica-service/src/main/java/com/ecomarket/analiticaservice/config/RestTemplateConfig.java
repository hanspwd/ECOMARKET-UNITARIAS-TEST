package com.ecomarket.analiticaservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate() {
        var factory = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var requestFactory = new org.springframework.http.client.JdkClientHttpRequestFactory(factory);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return new RestTemplate(requestFactory);
    }
}
