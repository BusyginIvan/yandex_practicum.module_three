package ru.yandex.practicum.bank.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class IntegrationClientsConfig {
    private static final String CLIENT_REGISTRATION_ID = "cash";
    private static final Authentication CASH_CLIENT_PRINCIPAL =
        new AnonymousAuthenticationToken(
            "cash-client",
            "cash-client",
            AuthorityUtils.createAuthorityList("ROLE_SYSTEM")
        );

    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public IntegrationClientsConfig(
        RestClient.Builder loadBalancedRestClientBuilder,
        OAuth2AuthorizedClientManager authorizedClientManager,
        OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.loadBalancedRestClientBuilder = loadBalancedRestClientBuilder;
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    RestClient accountsRestClient(@Value("${integration.accounts.base-url}") String baseUrl) {
        return oauth2RestClient(baseUrl);
    }

    @Bean
    RestClient notificationsRestClient(@Value("${integration.notifications.base-url}") String baseUrl) {
        return oauth2RestClient(baseUrl);
    }

    private RestClient oauth2RestClient(String baseUrl) {
        OAuth2ClientHttpRequestInterceptor oauth2Interceptor =
            new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        oauth2Interceptor.setClientRegistrationIdResolver(request -> CLIENT_REGISTRATION_ID);
        oauth2Interceptor.setPrincipalResolver(request -> CASH_CLIENT_PRINCIPAL);
        oauth2Interceptor.setAuthorizationFailureHandler(
            OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientService)
        );

        return loadBalancedRestClientBuilder
            .baseUrl(baseUrl)
            .requestInterceptor(oauth2Interceptor)
            .build();
    }
}
