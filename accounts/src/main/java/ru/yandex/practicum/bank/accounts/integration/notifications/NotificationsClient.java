package ru.yandex.practicum.bank.accounts.integration.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.bank.accounts.integration.notifications.model.NotificationsProfileUpdateRequest;

@Component
public class NotificationsClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationsClient.class);

    private final RestClient notificationsRestClient;

    public NotificationsClient(RestClient notificationsRestClient) {
        this.notificationsRestClient = notificationsRestClient;
    }

    public boolean sendProfileUpdateNotification(String operationId, String login) {
        try {
            notificationsRestClient.post()
                .uri("/profile")
                .header("Operation-Id", operationId)
                .body(new NotificationsProfileUpdateRequest(login))
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
