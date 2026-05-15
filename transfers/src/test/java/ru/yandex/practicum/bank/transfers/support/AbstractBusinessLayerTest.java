package ru.yandex.practicum.bank.transfers.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.bank.transfers.config.BusinessLayerTestConfig;
import ru.yandex.practicum.bank.transfers.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.transfers.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;
import ru.yandex.practicum.bank.transfers.service.CurrentAccountService;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = BusinessLayerTestConfig.class)
@TestPropertySource(properties = {
    "transfers.notifications.fixed-delay-ms=1000",
    "transfers.notifications.batch-size=10",
    "transfers.cleanup.cron=0 0 * * * *",
    "transfers.cleanup.batch-size=100",
    "transfers.retry.fixed-delay-ms=1000",
    "transfers.retry.batch-size=10"
})
public abstract class AbstractBusinessLayerTest {

    @Autowired protected AccountsClient accountsClient;
    @Autowired protected NotificationsClient notificationsClient;
    @Autowired protected TransferOperationRepository transferOperationRepository;
    @Autowired protected CurrentAccountService currentAccountService;

    @BeforeEach
    void resetMocks() {
        reset(
            accountsClient,
            notificationsClient,
            transferOperationRepository,
            currentAccountService
        );
    }
}
