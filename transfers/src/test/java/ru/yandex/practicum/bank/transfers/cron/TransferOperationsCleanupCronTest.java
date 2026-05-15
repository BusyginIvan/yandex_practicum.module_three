package ru.yandex.practicum.bank.transfers.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.transfers.support.AbstractBusinessLayerTest;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferOperationsCleanupCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private TransferOperationsCleanupCron transferOperationsCleanupCron;

    @Test
    void deleteOldCompletedOperationsDeletesInBatchesUntilLastBatchIsShort() {
        when(transferOperationRepository.deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(100, 100, 25);

        transferOperationsCleanupCron.deleteOldCompletedOperations();

        verify(transferOperationRepository, times(3))
            .deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100));
    }

    @Test
    void deleteOldCompletedOperationsStopsAfterFirstShortBatch() {
        when(transferOperationRepository.deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(25);

        transferOperationsCleanupCron.deleteOldCompletedOperations();

        verify(transferOperationRepository, times(1))
            .deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100));
    }
}
