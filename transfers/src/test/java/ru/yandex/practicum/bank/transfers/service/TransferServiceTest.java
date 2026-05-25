package ru.yandex.practicum.bank.transfers.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationResult;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.support.AbstractBusinessLayerTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTest extends AbstractBusinessLayerTest {
    @Autowired
    private TransferService transferService;

    @Test
    void performTransferCreatesNewOperationAndProcessesIt() {
        List<TransferOperationEntity> savedOperations = new ArrayList<>();

        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountsClient.performBalanceOperation(anyString(), anyString(), anyInt(), any()))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(transferOperationRepository.save(any(TransferOperationEntity.class))).thenAnswer(invocation -> {
            TransferOperationEntity operation = invocation.getArgument(0);
            savedOperations.add(copy(operation));
            return operation;
        });

        TransferOperationResult result = transferService.performTransfer(250, "bob");

        assertThat(result).isEqualTo(TransferOperationResult.SUCCESS);
        assertThat(savedOperations).hasSize(3);

        TransferOperationEntity created = savedOperations.get(0);
        TransferOperationEntity afterWithdraw = savedOperations.get(1);
        TransferOperationEntity afterDeposit = savedOperations.get(2);

        assertThat(created.getOperationId()).isNotBlank();
        assertThat(created.getSenderLogin()).isEqualTo("alice");
        assertThat(created.getRecipientLogin()).isEqualTo("bob");
        assertThat(created.getAmount()).isEqualTo(250);
        assertThat(created.getWithdrawOperationId()).isNotBlank();
        assertThat(created.getDepositOperationId()).isNotBlank();
        assertThat(created.getStage()).isEqualTo(TransferOperationStage.NEW);

        assertThat(afterWithdraw.getOperationId()).isEqualTo(created.getOperationId());
        assertThat(afterWithdraw.getStage()).isEqualTo(TransferOperationStage.WITHDRAW_SUCCEEDED);

        assertThat(afterDeposit.getOperationId()).isEqualTo(created.getOperationId());
        assertThat(afterDeposit.getStage()).isEqualTo(TransferOperationStage.NOTIFICATION_PENDING);

        verify(accountsClient).performBalanceOperation(
            created.getWithdrawOperationId(),
            "alice",
            250,
            BalanceOperationType.WITHDRAW
        );
        verify(accountsClient).performBalanceOperation(
            created.getDepositOperationId(),
            "bob",
            250,
            BalanceOperationType.DEPOSIT
        );
    }

    @Test
    void processOperationReturnsInsufficientFundsWhenWithdrawFails() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "poor-bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("withdraw-1", "alice", 100, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.INSUFFICIENT_FUNDS);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.INSUFFICIENT_FUNDS);
        assertThat(operation.getStage()).isEqualTo(TransferOperationStage.REJECTED_INSUFFICIENT_FUNDS);
        verify(transferOperationRepository).save(operation);
        verify(accountsClient, never()).performBalanceOperation("deposit-1", "poor-bob", 100, BalanceOperationType.DEPOSIT);
    }

    @Test
    void processOperationReturnsErrorWhenWithdrawCallFails() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("withdraw-1", "alice", 100, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.ERROR);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.ERROR);
        assertThat(operation.getStage()).isEqualTo(TransferOperationStage.NEW);
        verify(transferOperationRepository, never()).save(operation);
    }

    @Test
    void processOperationReturnsErrorWhenPersistingWithdrawStageFails() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("withdraw-1", "alice", 100, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.SUCCESS);
        doThrow(new RuntimeException("db failure")).when(transferOperationRepository).save(operation);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.ERROR);
    }

    @Test
    void processOperationReturnsErrorWhenDepositCallFailsAfterSuccessfulWithdraw() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.WITHDRAW_SUCCEEDED
        );
        when(accountsClient.performBalanceOperation("deposit-1", "bob", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.ERROR);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.ERROR);
        verify(transferOperationRepository, never()).save(operation);
    }

    @Test
    void processOperationMarksNotificationPendingAfterSuccessfulDeposit() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.WITHDRAW_SUCCEEDED
        );
        when(accountsClient.performBalanceOperation("deposit-1", "bob", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.SUCCESS);
        assertThat(operation.getStage()).isEqualTo(TransferOperationStage.NOTIFICATION_PENDING);
        verify(transferOperationRepository).save(operation);
    }

    @Test
    void processOperationReturnsErrorWhenPersistingTransferStageFails() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            100,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.WITHDRAW_SUCCEEDED
        );
        when(accountsClient.performBalanceOperation("deposit-1", "bob", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);
        doThrow(new RuntimeException("db failure")).when(transferOperationRepository).save(operation);

        TransferOperationResult result = transferService.processOperation(operation);

        assertThat(result).isEqualTo(TransferOperationResult.ERROR);
    }

    @Test
    void sendNotificationCompletesOperationWhenClientSucceeds() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            250,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendTransferNotification("operation-1", "alice", "bob", 250))
            .thenReturn(true);

        transferService.sendNotification(operation);

        assertThat(operation.getStage()).isEqualTo(TransferOperationStage.COMPLETED);
        verify(transferOperationRepository).save(operation);
    }

    @Test
    void sendNotificationDoesNothingWhenClientFails() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            250,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendTransferNotification("operation-1", "alice", "bob", 250))
            .thenReturn(false);

        transferService.sendNotification(operation);

        assertThat(operation.getStage()).isEqualTo(TransferOperationStage.NOTIFICATION_PENDING);
        verify(transferOperationRepository, never()).save(operation);
    }

    @Test
    void sendNotificationSwallowsPersistenceError() {
        TransferOperationEntity operation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            250,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendTransferNotification("operation-1", "alice", "bob", 250))
            .thenReturn(true);
        doThrow(new RuntimeException("db failure")).when(transferOperationRepository).save(operation);

        assertThatCode(() -> transferService.sendNotification(operation)).doesNotThrowAnyException();
    }

    private static TransferOperationEntity copy(TransferOperationEntity operation) {
        return new TransferOperationEntity(
            operation.getOperationId(),
            operation.getSenderLogin(),
            operation.getRecipientLogin(),
            operation.getAmount(),
            operation.getWithdrawOperationId(),
            operation.getDepositOperationId(),
            operation.getStage()
        );
    }
}
