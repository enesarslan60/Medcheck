package com.drugchecker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final int BUFFER_SIZE = 2 * 1024 * 1024; // 2 MB; openFDA labels can be large

    @Bean
    public WebClient rxNormWebClient(@Value("${rxnorm.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferStrategies())
                .build();
    }

    @Bean
    public WebClient openFdaWebClient(@Value("${openfda.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(largeBufferStrategies())
                .build();
    }

    private ExchangeStrategies largeBufferStrategies() {
        return ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(BUFFER_SIZE))
                .build();
    }
}
