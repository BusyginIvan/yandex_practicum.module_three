package ru.yandex.practicum.bank.cash.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.bank.cash.config.BusinessLayerTestConfig;
import ru.yandex.practicum.bank.cash.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.cash.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.service.CurrentAccountService;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = BusinessLayerTestConfig.class)
@TestPropertySource(properties = {
    "cash.notifications.fixed-delay-ms=1000",
    "cash.notifications.batch-size=10",
    "cash.cleanup.cron=0 0 * * * *",
    "cash.cleanup.batch-size=100",
    "cash.retry.fixed-delay-ms=1000",
    "cash.retry.batch-size=10"
})
public abstract class AbstractBusinessLayerTest {

    @Autowired protected AccountsClient accountsClient;
    @Autowired protected NotificationsClient notificationsClient;
    @Autowired protected CashOperationRepository cashOperationRepository;
    @Autowired protected CurrentAccountService currentAccountService;

    @BeforeEach
    void resetMocks() {
        reset(
            accountsClient,
            notificationsClient,
            cashOperationRepository,
            currentAccountService
        );
    }
}
