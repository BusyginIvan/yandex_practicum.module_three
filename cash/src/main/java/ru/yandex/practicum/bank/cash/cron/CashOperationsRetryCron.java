package ru.yandex.practicum.bank.cash.cron;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.service.CashService;

import java.time.Instant;
import java.util.List;

@Component
public class CashOperationsRetryCron {
    private final CashOperationRepository cashOperationRepository;
    private final CashService cashService;
    private final int retryBatchSize;

    public CashOperationsRetryCron(
        CashOperationRepository cashOperationRepository,
        CashService cashService,
        @Value("${cash.retry.batch-size}") int retryBatchSize
    ) {
        this.cashOperationRepository = cashOperationRepository;
        this.cashService = cashService;
        this.retryBatchSize = retryBatchSize;
    }

    @Scheduled(fixedDelayString = "${cash.retry.fixed-delay-ms}")
    public void retryCashOperations() {
        List<CashOperationEntity> operations = cashOperationRepository.findByStageCreatedBefore(
            CashOperationStage.NEW,
            Instant.now().minusSeconds(10),
            PageRequest.of(0, retryBatchSize)
        );
        for (CashOperationEntity operation : operations) {
            cashService.processOperation(operation);
        }
    }
}
