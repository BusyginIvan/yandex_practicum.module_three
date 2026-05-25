package ru.yandex.practicum.bank.transfers.cron;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;
import ru.yandex.practicum.bank.transfers.service.TransferService;

import java.util.List;

@Component
public class TransferNotificationsCron {
    private final TransferOperationRepository transferOperationRepository;
    private final TransferService transferService;
    private final int batchSize;

    public TransferNotificationsCron(
        TransferOperationRepository transferOperationRepository,
        TransferService transferService,
        @Value("${transfers.notifications.batch-size}") int batchSize
    ) {
        this.transferOperationRepository = transferOperationRepository;
        this.transferService = transferService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${transfers.notifications.fixed-delay-ms}")
    public void sendNotifications() {
        List<TransferOperationEntity> operations = transferOperationRepository.findByStageOrderByCreatedAtAsc(
            TransferOperationStage.NOTIFICATION_PENDING,
            PageRequest.of(0, batchSize)
        );
        for (TransferOperationEntity operation : operations) {
            transferService.sendNotification(operation);
        }
    }
}
