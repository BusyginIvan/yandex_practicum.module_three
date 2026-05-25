package ru.yandex.practicum.bank.cash.integration.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.integration.notifications.model.NotificationsCashOperationRequest;

@Component
public class NotificationsClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationsClient.class);
    private final RestClient notificationsRestClient;

    public NotificationsClient(RestClient notificationsRestClient) {
        this.notificationsRestClient = notificationsRestClient;
    }

    public boolean sendCashNotification(
        String operationId,
        String login,
        int amount,
        BalanceOperationType type
    ) {
        try {
            notificationsRestClient.post()
                .uri("/cash")
                .header("Operation-Id", operationId)
                .body(new NotificationsCashOperationRequest(login, type, amount))
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            log.error("Notification request failed with HTTP status {}", ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("Notification request failed", ex);
        }
        return false;
    }
}
