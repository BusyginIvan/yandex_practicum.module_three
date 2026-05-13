package ru.yandex.practicum.bank.accounts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.bank.accounts.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.accounts.persistence.repository.AccountRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.BalanceOperationRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.ProfileOperationRepository;
import ru.yandex.practicum.bank.accounts.service.CurrentAccountService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = {
    "ru.yandex.practicum.bank.accounts.service",
    "ru.yandex.practicum.bank.accounts.cron"
})
public class BusinessLayerTestConfig {

    @Bean
    public AccountRepository accountRepository() {
        return mock(AccountRepository.class);
    }

    @Bean
    public BalanceOperationRepository balanceOperationRepository() {
        return mock(BalanceOperationRepository.class);
    }

    @Bean
    public ProfileOperationRepository profileOperationRepository() {
        return mock(ProfileOperationRepository.class);
    }

    @Bean
    public NotificationsClient notificationsClient() {
        return mock(NotificationsClient.class);
    }

    @Bean @Primary
    public CurrentAccountService currentAccountService() {
        return mock(CurrentAccountService.class);
    }
}
