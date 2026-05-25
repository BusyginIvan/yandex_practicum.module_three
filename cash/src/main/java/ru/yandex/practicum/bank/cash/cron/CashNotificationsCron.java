package ru.yandex.practicum.bank.cash.cron;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.service.CashService;

import java.util.List;

@Component
public class CashNotificationsCron {
    private final CashOperationRepository cashOperationRepository;
    private final CashService cashService;
    private final int batchSize;

    public CashNotificationsCron(
        CashOperationRepository cashOperationRepository,
        CashService cashService,
        @Value("${cash.notifications.batch-size}") int batchSize
    ) {
        this.cashOperationRepository = cashOperationRepository;
        this.cashService = cashService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${cash.notifications.fixed-delay-ms}")
    public void sendNotifications() {
        List<CashOperationEntity> operations = cashOperationRepository.findByStageOrderByCreatedAtAsc(
            CashOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, batchSize)
        );
        for (CashOperationEntity operation : operations) {
            cashService.sendNotification(operation);
        }
    }
}
