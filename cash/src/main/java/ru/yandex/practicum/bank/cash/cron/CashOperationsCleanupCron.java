package ru.yandex.practicum.bank.cash.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;

import java.time.Instant;

@Component
public class CashOperationsCleanupCron {
    private static final Logger log = LoggerFactory.getLogger(CashOperationsCleanupCron.class);

    private final CashOperationRepository cashOperationRepository;
    private final int cleanupBatchSize;

    public CashOperationsCleanupCron(
        CashOperationRepository cashOperationRepository,
        @Value("${cash.cleanup.batch-size}") int cleanupBatchSize
    ) {
        this.cashOperationRepository = cashOperationRepository;
        this.cleanupBatchSize = cleanupBatchSize;
    }

    @Scheduled(cron = "${cash.cleanup.cron}")
    public void deleteOldCompletedOperations() {
        Instant createdBefore = Instant.now().minusSeconds(24 * 60 * 60);

        int deletedOperations = 0;
        int deletedBatchSize;
        do {
            deletedBatchSize = cashOperationRepository.deleteFinalOperationsCreatedBefore(
                createdBefore,
                cleanupBatchSize
            );
            deletedOperations += deletedBatchSize;
        } while (deletedBatchSize == cleanupBatchSize);

        if (deletedOperations > 0) {
            log.info("Deleted {} old completed cash operations created before {}", deletedOperations, createdBefore);
        }
    }
}
