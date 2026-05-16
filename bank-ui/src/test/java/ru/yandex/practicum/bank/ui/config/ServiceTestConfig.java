package ru.yandex.practicum.bank.ui.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.bank.ui.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.ui.integration.cash.CashClient;
import ru.yandex.practicum.bank.ui.integration.transfers.TransfersClient;
import ru.yandex.practicum.bank.ui.service.CurrentAccountService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.bank.ui.service")
public class ServiceTestConfig {

    @Bean
    public AccountsClient accountsClient() {
        return mock(AccountsClient.class);
    }

    @Bean
    public CashClient cashClient() {
        return mock(CashClient.class);
    }

    @Bean
    public TransfersClient transfersClient() {
        return mock(TransfersClient.class);
    }

    @Bean
    @Primary
    public CurrentAccountService currentAccountService() {
        return mock(CurrentAccountService.class);
    }
}
