package ru.yandex.practicum.bank.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.notifications.domain.CashOperationType;
import ru.yandex.practicum.bank.notifications.persistence.repository.ProcessedNotificationRepository;

@Service
public class NotificationsService {
    private static final Logger log = LoggerFactory.getLogger(NotificationsService.class);

    private final ProcessedNotificationRepository processedNotificationRepository;

    public NotificationsService(ProcessedNotificationRepository processedNotificationRepository) {
        this.processedNotificationRepository = processedNotificationRepository;
    }

    public void sendCashOperationNotification(
        String operationId,
        String login,
        CashOperationType type,
        int amount
    ) {
        if (processedNotificationRepository.createIfMissing(operationId) == 0) return;

        log.info(
            "Notification for user {}: {} {} rubles.",
            login,
            type == CashOperationType.DEPOSIT ? "deposited" : "withdrew",
            amount
        );
    }
}
