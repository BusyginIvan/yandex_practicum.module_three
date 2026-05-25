package ru.yandex.practicum.bank.transfers.integration.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.transfers.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationsClientContractTest extends AbstractClientContractTest {
    @Autowired
    private NotificationsClient notificationsClient;

    @Test
    void sendTransferNotificationReturnsTrue() {
        boolean result = notificationsClient.sendTransferNotification(
            "transfer-notification-success",
            "alice",
            "bob",
            250
        );

        assertThat(result).isTrue();
    }
}
