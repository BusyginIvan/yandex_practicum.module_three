package ru.yandex.practicum.bank.accounts.cron;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;
import ru.yandex.practicum.bank.accounts.persistence.repository.ProfileOperationRepository;
import ru.yandex.practicum.bank.accounts.service.AccountsService;

import java.util.List;

@Component
public class ProfileUpdateNotificationsCron {
    private final ProfileOperationRepository profileOperationRepository;
    private final AccountsService accountsService;
    private final int batchSize;

    public ProfileUpdateNotificationsCron(
        ProfileOperationRepository profileOperationRepository,
        AccountsService accountsService,
        @Value("${accounts.notifications.batch-size}") int batchSize
    ) {
        this.profileOperationRepository = profileOperationRepository;
        this.accountsService = accountsService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${accounts.notifications.fixed-delay-ms}")
    public void sendNotifications() {
        List<ProfileOperationEntity> operations =
            profileOperationRepository.findByStageOrderByCreatedAtAsc(
                ProfileOperationStage.NOTIFICATION_PENDING,
                PageRequest.of(0, batchSize)
            );
        for (ProfileOperationEntity operation : operations) {
            accountsService.sendProfileUpdateNotification(operation);
        }
    }
}
