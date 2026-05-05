package ru.yandex.practicum.bank.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class IntegrationClientsConfig {
    private final RestClient.Builder loadBalancedRestClientBuilder;

    public IntegrationClientsConfig(
        RestClient.Builder loadBalancedRestClientBuilder
    ) {
        this.loadBalancedRestClientBuilder = loadBalancedRestClientBuilder;
    }

    @Bean
    RestClient accountsRestClient(@Value("${integration.accounts.base-url}") String baseUrl) {
        return oauth2RestClient(baseUrl);
    }

    private RestClient oauth2RestClient(String baseUrl) {
        return loadBalancedRestClientBuilder
            .baseUrl(baseUrl)
            .build();
    }
}
