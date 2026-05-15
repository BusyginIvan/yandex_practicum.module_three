package ru.yandex.practicum.bank.transfers.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.support.AbstractBusinessLayerTest;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferOperationsRetryCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private TransferOperationsRetryCron transferOperationsRetryCron;

    @Test
    void retryTransferOperationsLoadsRetryableOperationsAndProcessesThem() {
        TransferOperationEntity firstOperation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            250,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NEW
        );
        TransferOperationEntity secondOperation = new TransferOperationEntity(
            "operation-2",
            "carol",
            "dave",
            120,
            "withdraw-2",
            "deposit-2",
            TransferOperationStage.WITHDRAW_SUCCEEDED
        );

        when(transferOperationRepository.findByStageInCreatedBefore(
            eq(List.of(TransferOperationStage.NEW, TransferOperationStage.WITHDRAW_SUCCEEDED)),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        )).thenReturn(List.of(firstOperation, secondOperation));
        when(accountsClient.performBalanceOperation("withdraw-1", "alice", 250, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(accountsClient.performBalanceOperation("deposit-1", "bob", 250, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(accountsClient.performBalanceOperation("deposit-2", "dave", 120, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);

        transferOperationsRetryCron.retryTransferOperations();

        verify(transferOperationRepository).findByStageInCreatedBefore(
            eq(List.of(TransferOperationStage.NEW, TransferOperationStage.WITHDRAW_SUCCEEDED)),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        );
        verify(accountsClient).performBalanceOperation("withdraw-1", "alice", 250, BalanceOperationType.WITHDRAW);
        verify(accountsClient).performBalanceOperation("deposit-1", "bob", 250, BalanceOperationType.DEPOSIT);
        verify(accountsClient).performBalanceOperation("deposit-2", "dave", 120, BalanceOperationType.DEPOSIT);
        verify(transferOperationRepository, times(2)).save(firstOperation);
        verify(transferOperationRepository).save(secondOperation);
    }

    @Test
    void retryTransferOperationsDoesNothingWhenNoRetryableOperations() {
        when(transferOperationRepository.findByStageInCreatedBefore(
            eq(List.of(TransferOperationStage.NEW, TransferOperationStage.WITHDRAW_SUCCEEDED)),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        )).thenReturn(List.of());

        transferOperationsRetryCron.retryTransferOperations();

        verify(transferOperationRepository).findByStageInCreatedBefore(
            eq(List.of(TransferOperationStage.NEW, TransferOperationStage.WITHDRAW_SUCCEEDED)),
            any(Instant.class),
            eq(PageRequest.of(0, 10))
        );
        verify(accountsClient, never()).performBalanceOperation(
            anyString(),
            anyString(),
            anyInt(),
            any()
        );
    }
}
