package ru.yandex.practicum.bank.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class IntegrationClientsConfig {
    private static final String CLIENT_REGISTRATION_ID = "keycloak";

    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public IntegrationClientsConfig(
        RestClient.Builder loadBalancedRestClientBuilder,
        OAuth2AuthorizedClientManager authorizedClientManager,
        OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        this.loadBalancedRestClientBuilder = loadBalancedRestClientBuilder;
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Bean
    RestClient accountsRestClient(@Value("${integration.accounts.base-url}") String baseUrl) {
        return oauth2RestClient(baseUrl);
    }

    @Bean
    RestClient cashRestClient(@Value("${integration.cash.base-url}") String baseUrl) {
        return oauth2RestClient(baseUrl);
    }

    private RestClient oauth2RestClient(String baseUrl) {
        OAuth2ClientHttpRequestInterceptor oauth2Interceptor =
            new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        oauth2Interceptor.setClientRegistrationIdResolver(request -> CLIENT_REGISTRATION_ID);
        oauth2Interceptor.setAuthorizationFailureHandler(
            OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientRepository)
        );

        return loadBalancedRestClientBuilder
            .baseUrl(baseUrl)
            .requestInterceptor(oauth2Interceptor)
            .build();
    }
}
