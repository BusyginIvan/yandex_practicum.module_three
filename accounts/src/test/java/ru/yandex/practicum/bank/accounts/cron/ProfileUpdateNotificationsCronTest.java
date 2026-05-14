package ru.yandex.practicum.bank.accounts.cron;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;
import ru.yandex.practicum.bank.accounts.support.AbstractBusinessLayerTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileUpdateNotificationsCronTest extends AbstractBusinessLayerTest {
    @Autowired
    private ProfileUpdateNotificationsCron profileUpdateNotificationsCron;

    @Test
    void sendNotificationsLoadsPendingOperationsAndSendsNotifications() {
        ProfileOperationEntity firstOperation = new ProfileOperationEntity(
            "operation-1",
            "alice",
            ProfileOperationStage.NOTIFICATION_PENDING
        );
        ProfileOperationEntity secondOperation = new ProfileOperationEntity(
            "operation-2",
            "bob",
            ProfileOperationStage.NOTIFICATION_PENDING
        );

        when(profileOperationRepository.findByStageOrderByCreatedAtAsc(
            ProfileOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of(firstOperation, secondOperation));

        when(notificationsClient.sendProfileUpdateNotification("operation-1", "alice")).thenReturn(false);
        when(notificationsClient.sendProfileUpdateNotification("operation-2", "bob")).thenReturn(false);

        profileUpdateNotificationsCron.sendNotifications();

        verify(profileOperationRepository).findByStageOrderByCreatedAtAsc(
            ProfileOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient).sendProfileUpdateNotification("operation-1", "alice");
        verify(notificationsClient).sendProfileUpdateNotification("operation-2", "bob");
    }

    @Test
    void sendNotificationsDoesNothingWhenNoPendingOperations() {
        when(profileOperationRepository.findByStageOrderByCreatedAtAsc(
            ProfileOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        )).thenReturn(List.of());

        profileUpdateNotificationsCron.sendNotifications();

        verify(profileOperationRepository).findByStageOrderByCreatedAtAsc(
            ProfileOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, 10)
        );
        verify(notificationsClient, never()).sendProfileUpdateNotification(anyString(), anyString());
    }
}
