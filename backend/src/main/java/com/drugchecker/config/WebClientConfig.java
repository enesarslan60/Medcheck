package com.drugchecker.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024;

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

    @Bean
    public WebClient anthropicWebClient(
            @Value("${anthropic.base-url}") String baseUrl,
            @Value("${anthropic.api-key:}") String apiKey,
            @Value("${anthropic.timeout-seconds:15}") int timeoutSeconds) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("anthropic-version", "2023-06-01")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(largeBufferStrategies());

        if (apiKey != null && !apiKey.isBlank()) {
            builder = builder.defaultHeader("x-api-key", apiKey);
        }
        return builder.build();
    }

    @Bean
    public WebClient geminiWebClient(
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${gemini.timeout-seconds:15}") int timeoutSeconds) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(largeBufferStrategies())
                .build();
    }

    private ExchangeStrategies largeBufferStrategies() {
        return ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(BUFFER_SIZE))
                .build();
    }
}
