package ru.yandex.practicum.bank.cash.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.support.AbstractBusinessLayerTest;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashOperationsRetryCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private CashOperationsRetryCron cashOperationsRetryCron;

    @Test
    void retryCashOperationsLoadsExpiredNewOperationsAndProcessesThem() {
        CashOperationEntity firstOperation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NEW
        );
        CashOperationEntity secondOperation = new CashOperationEntity(
            "operation-2",
            "bob",
            BalanceOperationType.WITHDRAW,
            50,
            CashOperationStage.NEW
        );

        when(cashOperationRepository.findByStageCreatedBefore(
            eq(CashOperationStage.NEW),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        )).thenReturn(List.of(firstOperation, secondOperation));
        when(accountsClient.performBalanceOperation("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(accountsClient.performBalanceOperation("operation-2", "bob", 50, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.INSUFFICIENT_FUNDS);

        cashOperationsRetryCron.retryCashOperations();

        verify(cashOperationRepository).findByStageCreatedBefore(
            eq(CashOperationStage.NEW),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        );
        verify(accountsClient).performBalanceOperation("operation-1", "alice", 100, BalanceOperationType.DEPOSIT);
        verify(accountsClient).performBalanceOperation("operation-2", "bob", 50, BalanceOperationType.WITHDRAW);
        verify(cashOperationRepository).save(firstOperation);
        verify(cashOperationRepository).save(secondOperation);
    }

    @Test
    void retryCashOperationsDoesNothingWhenNoExpiredOperations() {
        when(cashOperationRepository.findByStageCreatedBefore(
            eq(CashOperationStage.NEW),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        )).thenReturn(List.of());

        cashOperationsRetryCron.retryCashOperations();

        verify(cashOperationRepository).findByStageCreatedBefore(
            eq(CashOperationStage.NEW),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        );
        verify(accountsClient, never()).performBalanceOperation(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            anyInt(),
            any()
        );
    }
}
