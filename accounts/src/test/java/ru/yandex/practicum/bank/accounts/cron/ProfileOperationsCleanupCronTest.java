package ru.yandex.practicum.bank.accounts.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.accounts.support.AbstractBusinessLayerTest;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileOperationsCleanupCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private ProfileOperationsCleanupCron profileOperationsCleanupCron;

    @Test
    void deleteOldCompletedOperationsDeletesInBatchesUntilLastBatchIsShort() {
        when(profileOperationRepository.deleteCompletedOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(100, 100, 25);

        profileOperationsCleanupCron.deleteOldCompletedOperations();

        verify(profileOperationRepository, times(3))
            .deleteCompletedOperationsCreatedBefore(any(Instant.class), eq(100));
    }

    @Test
    void deleteOldCompletedOperationsStopsAfterFirstShortBatch() {
        when(profileOperationRepository.deleteCompletedOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(25);

        profileOperationsCleanupCron.deleteOldCompletedOperations();

        verify(profileOperationRepository, times(1))
            .deleteCompletedOperationsCreatedBefore(any(Instant.class), eq(100));
    }
}
