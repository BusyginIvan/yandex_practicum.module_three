package ru.yandex.practicum.bank.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AccountsClientConfig {
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient accountsRestClient(
        @LoadBalanced RestClient.Builder restClientBuilder,
        @Value("${integration.accounts.base-url}") String baseUrl
    ) {
        return restClientBuilder
            .baseUrl(baseUrl)
            .build();
    }
}
