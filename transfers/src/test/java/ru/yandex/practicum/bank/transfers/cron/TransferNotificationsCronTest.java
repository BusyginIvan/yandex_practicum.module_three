package ru.yandex.practicum.bank.transfers.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.support.AbstractBusinessLayerTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferNotificationsCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private TransferNotificationsCron transferNotificationsCron;

    @Test
    void sendNotificationsLoadsPendingOperationsAndSendsNotifications() {
        TransferOperationEntity firstOperation = new TransferOperationEntity(
            "operation-1",
            "alice",
            "bob",
            250,
            "withdraw-1",
            "deposit-1",
            TransferOperationStage.NOTIFICATION_PENDING
        );
        TransferOperationEntity secondOperation = new TransferOperationEntity(
            "operation-2",
            "carol",
            "dave",
            120,
            "withdraw-2",
            "deposit-2",
            TransferOperationStage.NOTIFICATION_PENDING
        );

        when(transferOperationRepository.findByStageOrderByCreatedAtAsc(
            TransferOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of(firstOperation, secondOperation));
        when(notificationsClient.sendTransferNotification("operation-1", "alice", "bob", 250)).thenReturn(true);
        when(notificationsClient.sendTransferNotification("operation-2", "carol", "dave", 120)).thenReturn(false);

        transferNotificationsCron.sendNotifications();

        verify(transferOperationRepository).findByStageOrderByCreatedAtAsc(
            TransferOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient).sendTransferNotification("operation-1", "alice", "bob", 250);
        verify(notificationsClient).sendTransferNotification("operation-2", "carol", "dave", 120);
        verify(transferOperationRepository).save(firstOperation);
        verify(transferOperationRepository, never()).save(secondOperation);
    }

    @Test
    void sendNotificationsDoesNothingWhenNoPendingOperations() {
        when(transferOperationRepository.findByStageOrderByCreatedAtAsc(
            TransferOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of());

        transferNotificationsCron.sendNotifications();

        verify(transferOperationRepository).findByStageOrderByCreatedAtAsc(
            TransferOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient, never()).sendTransferNotification(anyString(), anyString(), anyString(), anyInt());
    }
}
