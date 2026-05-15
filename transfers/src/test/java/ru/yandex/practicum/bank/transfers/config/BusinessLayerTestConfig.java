package ru.yandex.practicum.bank.transfers.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.bank.transfers.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.transfers.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;
import ru.yandex.practicum.bank.transfers.service.CurrentAccountService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.transfers.service",
    "ru.yandex.practicum.bank.transfers.cron"
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
    public TransferOperationRepository transferOperationRepository() {
        return mock(TransferOperationRepository.class);
    }

    @Bean @Primary
    public CurrentAccountService currentAccountService() {
        return mock(CurrentAccountService.class);
    }
}
