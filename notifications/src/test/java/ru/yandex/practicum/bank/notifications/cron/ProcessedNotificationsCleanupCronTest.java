package ru.yandex.practicum.bank.notifications.cron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.bank.notifications.config.BusinessLayerTestConfig;
import ru.yandex.practicum.bank.notifications.persistence.repository.ProcessedNotificationRepository;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = BusinessLayerTestConfig.class)
@TestPropertySource(properties = {
    "notifications.cleanup.cron=0 0 * * * *",
    "notifications.cleanup.batch-size=100"
})
class ProcessedNotificationsCleanupCronTest {
    @Autowired
    private ProcessedNotificationsCleanupCron processedNotificationsCleanupCron;

    @Autowired
    private ProcessedNotificationRepository processedNotificationRepository;

    @BeforeEach
    void beforeEach() {
        reset(processedNotificationRepository);
    }

    @Test
    void deleteOldProcessedNotificationsDeletesInBatchesUntilLastBatchIsShort() {
        when(processedNotificationRepository.deleteCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(100, 100, 25);

        processedNotificationsCleanupCron.deleteOldProcessedNotifications();

        verify(processedNotificationRepository, times(3))
            .deleteCreatedBefore(any(Instant.class), eq(100));
    }

    @Test
    void deleteOldProcessedNotificationsStopsAfterFirstShortBatch() {
        when(processedNotificationRepository.deleteCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(25);

        processedNotificationsCleanupCron.deleteOldProcessedNotifications();

        verify(processedNotificationRepository, times(1))
            .deleteCreatedBefore(any(Instant.class), eq(100));
    }
}
