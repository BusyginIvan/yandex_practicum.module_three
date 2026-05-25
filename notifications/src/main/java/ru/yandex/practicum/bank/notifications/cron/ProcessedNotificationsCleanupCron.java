package ru.yandex.practicum.bank.notifications.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.notifications.persistence.repository.ProcessedNotificationRepository;

import java.time.Instant;

@Component
public class ProcessedNotificationsCleanupCron {
    private static final Logger log = LoggerFactory.getLogger(ProcessedNotificationsCleanupCron.class);

    private final ProcessedNotificationRepository processedNotificationRepository;
    private final int cleanupBatchSize;

    public ProcessedNotificationsCleanupCron(
        ProcessedNotificationRepository processedNotificationRepository,
        @Value("${notifications.cleanup.batch-size}") int cleanupBatchSize
    ) {
        this.processedNotificationRepository = processedNotificationRepository;
        this.cleanupBatchSize = cleanupBatchSize;
    }

    @Scheduled(cron = "${notifications.cleanup.cron}")
    public void deleteOldProcessedNotifications() {
        Instant createdBefore = Instant.now().minusSeconds(24 * 60 * 60);

        int deletedNotifications = 0;
        int deletedBatchSize;
        do {
            deletedBatchSize = processedNotificationRepository.deleteCreatedBefore(
                createdBefore,
                cleanupBatchSize
            );
            deletedNotifications += deletedBatchSize;
        } while (deletedBatchSize == cleanupBatchSize);

        if (deletedNotifications > 0) {
            log.info(
                "Deleted {} processed notifications created before {}",
                deletedNotifications,
                createdBefore
            );
        }
    }
}
