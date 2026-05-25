package ru.yandex.practicum.bank.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = "ru.yandex.practicum.bank.cash.integration")
public class ClientContractTestConfig {
    private final RestClient.Builder restClientBuilder;

    public ClientContractTestConfig(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
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
        return restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer stub-token")
            .build();
    }
}
