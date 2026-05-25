package ru.yandex.practicum.bank.transfers.cron;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;
import ru.yandex.practicum.bank.transfers.service.TransferService;

import java.time.Instant;
import java.util.List;

@Component
public class TransferOperationsRetryCron {
    private static final List<TransferOperationStage> RETRYABLE_STAGES = List.of(
        TransferOperationStage.NEW,
        TransferOperationStage.WITHDRAW_SUCCEEDED
    );

    private final TransferOperationRepository transferOperationRepository;
    private final TransferService transferService;
    private final int retryBatchSize;

    public TransferOperationsRetryCron(
        TransferOperationRepository transferOperationRepository,
        TransferService transferService,
        @Value("${transfers.retry.batch-size}") int retryBatchSize
    ) {
        this.transferOperationRepository = transferOperationRepository;
        this.transferService = transferService;
        this.retryBatchSize = retryBatchSize;
    }

    @Scheduled(fixedDelayString = "${transfers.retry.fixed-delay-ms}")
    public void retryTransferOperations() {
        List<TransferOperationEntity> operations = transferOperationRepository.findByStageInCreatedBefore(
            RETRYABLE_STAGES,
            Instant.now().minusSeconds(10),
            PageRequest.of(0, retryBatchSize)
        );
        for (TransferOperationEntity operation : operations) {
            transferService.processOperation(operation);
        }
    }
}
