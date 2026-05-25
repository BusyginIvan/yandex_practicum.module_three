package ru.yandex.practicum.bank.cash.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.bank.cash.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.cash.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.service.CurrentAccountService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.cash.service",
    "ru.yandex.practicum.bank.cash.cron"
})
public class BusinessLayerTestConfig {

    @Bean
    public AccountsClient accountsClient() {
        return mock(AccountsClient.class);
    }

    @Bean
    public NotificationsClient notificationsClient() {
        return mock(NotificationsClient.class);
    }

    @Bean
    public CashOperationRepository cashOperationRepository() {
        return mock(CashOperationRepository.class);
    }

    @Bean
    @Primary
    public CurrentAccountService currentAccountService() {
        return mock(CurrentAccountService.class);
    }
}
