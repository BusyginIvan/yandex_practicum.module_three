package ru.yandex.practicum.bank.cash.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.cash.support.AbstractBusinessLayerTest;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashOperationsCleanupCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private CashOperationsCleanupCron cashOperationsCleanupCron;

    @Test
    void deleteOldCompletedOperationsDeletesInBatchesUntilLastBatchIsShort() {
        when(cashOperationRepository.deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(100, 100, 25);

        cashOperationsCleanupCron.deleteOldCompletedOperations();

        verify(cashOperationRepository, times(3))
            .deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100));
    }

    @Test
    void deleteOldCompletedOperationsStopsAfterFirstShortBatch() {
        when(cashOperationRepository.deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100)))
            .thenReturn(25);

        cashOperationsCleanupCron.deleteOldCompletedOperations();

        verify(cashOperationRepository, times(1))
            .deleteFinalOperationsCreatedBefore(any(Instant.class), eq(100));
    }
}
