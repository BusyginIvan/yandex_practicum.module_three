package ru.yandex.practicum.bank.cash.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.support.AbstractBusinessLayerTest;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashNotificationsCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private CashNotificationsCron cashNotificationsCron;

    @Test
    void sendNotificationsLoadsPendingOperationsAndSendsNotifications() {
        CashOperationEntity firstOperation = new CashOperationEntity(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            CashOperationStage.NOTIFICATION_PENDING
        );
        CashOperationEntity secondOperation = new CashOperationEntity(
            "operation-2",
            "bob",
            BalanceOperationType.WITHDRAW,
            50,
            CashOperationStage.NOTIFICATION_PENDING
        );

        when(cashOperationRepository.findByStageOrderByCreatedAtAsc(
            CashOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of(firstOperation, secondOperation));
        when(notificationsClient.sendCashNotification("operation-1", "alice", 100, BalanceOperationType.DEPOSIT))
            .thenReturn(true);
        when(notificationsClient.sendCashNotification("operation-2", "bob", 50, BalanceOperationType.WITHDRAW))
            .thenReturn(false);

        cashNotificationsCron.sendNotifications();

        verify(cashOperationRepository).findByStageOrderByCreatedAtAsc(
            CashOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient).sendCashNotification("operation-1", "alice", 100, BalanceOperationType.DEPOSIT);
        verify(notificationsClient).sendCashNotification("operation-2", "bob", 50, BalanceOperationType.WITHDRAW);
        verify(cashOperationRepository).save(firstOperation);
        verify(cashOperationRepository, never()).save(secondOperation);
    }

    @Test
    void sendNotificationsDoesNothingWhenNoPendingOperations() {
        when(cashOperationRepository.findByStageOrderByCreatedAtAsc(
            CashOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of());

        cashNotificationsCron.sendNotifications();

        verify(cashOperationRepository).findByStageOrderByCreatedAtAsc(
            CashOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient, never()).sendCashNotification(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.any()
        );
    }
}
