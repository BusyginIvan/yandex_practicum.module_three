package ru.yandex.practicum.bank.accounts.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.bank.accounts.config.BusinessLayerTestConfig;
import ru.yandex.practicum.bank.accounts.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.accounts.persistence.repository.AccountRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.BalanceOperationRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.ProfileOperationRepository;
import ru.yandex.practicum.bank.accounts.service.CurrentAccountService;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = BusinessLayerTestConfig.class)
public abstract class AbstractBusinessLayerTest {

    @Autowired protected AccountRepository accountRepository;
    @Autowired protected BalanceOperationRepository balanceOperationRepository;
    @Autowired protected ProfileOperationRepository profileOperationRepository;
    @Autowired protected NotificationsClient notificationsClient;
    @Autowired protected CurrentAccountService currentAccountService;

    @BeforeEach
    void resetMocks() {
        reset(
            accountRepository,
            balanceOperationRepository,
            profileOperationRepository,
            notificationsClient,
            currentAccountService
        );
    }
}
