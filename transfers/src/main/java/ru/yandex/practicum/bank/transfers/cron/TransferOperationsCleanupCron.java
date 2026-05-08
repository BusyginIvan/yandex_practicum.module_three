package ru.yandex.practicum.bank.transfers.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;

import java.time.Instant;

@Component
public class TransferOperationsCleanupCron {
    private static final Logger log = LoggerFactory.getLogger(TransferOperationsCleanupCron.class);

    private final TransferOperationRepository transferOperationRepository;
    private final int cleanupBatchSize;

    public TransferOperationsCleanupCron(
        TransferOperationRepository transferOperationRepository,
        @Value("${transfers.cleanup.batch-size}") int cleanupBatchSize
    ) {
        this.transferOperationRepository = transferOperationRepository;
        this.cleanupBatchSize = cleanupBatchSize;
    }

    @Scheduled(cron = "${transfers.cleanup.cron}")
    public void deleteOldCompletedOperations() {
        Instant createdBefore = Instant.now().minusSeconds(24 * 60 * 60);

        int deletedOperations = 0;
        int deletedBatchSize;
        do {
            deletedBatchSize = transferOperationRepository.deleteFinalOperationsCreatedBefore(
                createdBefore,
                cleanupBatchSize
            );
            deletedOperations += deletedBatchSize;
        } while (deletedBatchSize == cleanupBatchSize);

        if (deletedOperations > 0) {
            log.info(
                "Deleted {} old completed transfer operations created before {}",
                deletedOperations,
                createdBefore
            );
        }
    }
}
