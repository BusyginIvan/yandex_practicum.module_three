package ru.yandex.practicum.bank.cash.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.support.AbstractBusinessLayerTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashServiceTest extends AbstractBusinessLayerTest {
    @Autowired
    private CashService cashService;

    @Test
    void performCashOperationCreatesNewOperationAndProcessesIt() {
        List<CashOperationEntity> savedOperations = new ArrayList<>();

        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountsClient.performBalanceOperation(any(), any(), anyInt(), any()))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(cashOperationRepository.save(any(CashOperationEntity.class))).thenAnswer(invocation -> {
            CashOperationEntity operation = invocation.getArgument(0);
            savedOperations.add(new CashOperationEntity(
                operation.getOperationId(),
                operation.getLogin(),
                operation.getType(),
                operation.getAmount(),
                operation.getStage()
            ));
            return operation;
        });

        BalanceOperationResult result = cashService.performCashOperation(100, BalanceOperationType.DEPOSIT);

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
        assertThat(savedOperations).hasSize(2);

        CashOperationEntity createdOperation = savedOperations.getFirst();
        CashOperationEntity updatedOperation = savedOperations.getLast();
        assertThat(createdOperation.getOperationId()).isNotBlank();
        assertThat(createdOperation.getLogin()).isEqualTo("alice");
        assertThat(createdOperation.getType()).isEqualTo(BalanceOperationType.DEPOSIT);
        assertThat(createdOperation.getAmount()).isEqualTo(100);
        assertThat(createdOperation.getStage()).isEqualTo(CashOperationStage.NEW);
        assertThat(updatedOperation.getOperationId()).isEqualTo(createdOperation.getOperationId());
        assertThat(updatedOperation.getStage()).isEqualTo(CashOperationStage.NOTIFICATION_PENDING);

        verify(accountsClient).performBalanceOperation(
            createdOperation.getOperationId(),
            "alice",
            100,
            BalanceOperationType.DEPOSIT
        );
    }

    @Test
    void processOperationMarksOperationForNotificationAfterSuccessfulBalanceChange() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);

        BalanceOperationResult result = cashService.processOperation(operation);

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
        assertThat(operation.getStage()).isEqualTo(CashOperationStage.NOTIFICATION_PENDING);
        verify(cashOperationRepository).save(operation);
    }

    @Test
    void processOperationMarksInsufficientFundsRejection() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.WITHDRAW,
            100,
            CashOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("operation-1", "alice", 100, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.INSUFFICIENT_FUNDS);

        BalanceOperationResult result = cashService.processOperation(operation);

        assertThat(result).isEqualTo(BalanceOperationResult.INSUFFICIENT_FUNDS);
        assertThat(operation.getStage()).isEqualTo(CashOperationStage.REJECTED_INSUFFICIENT_FUNDS);
        verify(cashOperationRepository).save(operation);
    }

    @Test
    void processOperationReturnsErrorWhenPersistingUpdatedStateFails() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NEW
        );
        when(accountsClient.performBalanceOperation("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.SUCCESS);
        doThrow(new RuntimeException("db failure")).when(cashOperationRepository).save(operation);

        BalanceOperationResult result = cashService.processOperation(operation);

        assertThat(result).isEqualTo(BalanceOperationResult.ERROR);
    }

    @Test
    void sendNotificationCompletesOperationWhenClientSucceeds() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendCashNotification("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(true);

        boolean sent = cashService.sendNotification(operation);

        assertThat(sent).isTrue();
        assertThat(operation.getStage()).isEqualTo(CashOperationStage.COMPLETED);
        verify(cashOperationRepository).save(operation);
    }

    @Test
    void sendNotificationDoesNothingWhenClientFails() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendCashNotification("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(false);

        boolean sent = cashService.sendNotification(operation);

        assertThat(sent).isFalse();
        assertThat(operation.getStage()).isEqualTo(CashOperationStage.NOTIFICATION_PENDING);
        verify(cashOperationRepository, never()).save(operation);
    }

    @Test
    void sendNotificationReturnsFalseWhenPersistingUpdatedStateFails() {
        CashOperationEntity operation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendCashNotification("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(true);
        doThrow(new RuntimeException("db failure")).when(cashOperationRepository).save(operation);

        boolean sent = cashService.sendNotification(operation);

        assertThat(sent).isFalse();
    }
}
