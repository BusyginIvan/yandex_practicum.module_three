package ru.yandex.practicum.bank.ui.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.bank.ui.config.ServiceTestConfig;
import ru.yandex.practicum.bank.ui.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.ui.integration.cash.CashClient;
import ru.yandex.practicum.bank.ui.integration.transfers.TransfersClient;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = ServiceTestConfig.class)
public abstract class AbstractServiceTest {

    @Autowired protected AccountsClient accountsClient;
    @Autowired protected CashClient cashClient;
    @Autowired protected TransfersClient transfersClient;
    @Autowired protected CurrentAccountService currentAccountService;

    @BeforeEach
    void resetMocks() {
        reset(
            accountsClient,
            cashClient,
            transfersClient,
            currentAccountService
        );
    }
}
