package ru.yandex.practicum.bank.cash.integration.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationsClientContractTest extends AbstractClientContractTest {

    @Autowired
    private NotificationsClient notificationsClient;

    @Test
    void sendCashNotificationShouldMatchContract() {
        boolean result = notificationsClient.sendCashNotification(
            "cash-operation-success",
            "alice",
            100,
            BalanceOperationType.DEPOSIT
        );

        assertThat(result).isTrue();
    }
}
