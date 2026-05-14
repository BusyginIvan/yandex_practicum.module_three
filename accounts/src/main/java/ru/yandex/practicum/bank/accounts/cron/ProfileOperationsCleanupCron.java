package ru.yandex.practicum.bank.accounts.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.accounts.persistence.repository.ProfileOperationRepository;

import java.time.Instant;

@Component
public class ProfileOperationsCleanupCron {
    private static final Logger log = LoggerFactory.getLogger(ProfileOperationsCleanupCron.class);

    private final ProfileOperationRepository profileOperationRepository;
    private final int cleanupBatchSize;

    public ProfileOperationsCleanupCron(
        ProfileOperationRepository profileOperationRepository,
        @Value("${accounts.cleanup.batch-size}") int cleanupBatchSize
    ) {
        this.profileOperationRepository = profileOperationRepository;
        this.cleanupBatchSize = cleanupBatchSize;
    }

    @Scheduled(cron = "${accounts.cleanup.cron}")
    public void deleteOldCompletedOperations() {
        Instant createdBefore = Instant.now().minusSeconds(24 * 60 * 60);

        int deletedOperations = 0;
        int deletedBatchSize;
        do {
            deletedBatchSize = profileOperationRepository.deleteCompletedOperationsCreatedBefore(
                createdBefore,
                cleanupBatchSize
            );
            deletedOperations += deletedBatchSize;
        } while (deletedBatchSize == cleanupBatchSize);

        if (deletedOperations > 0) {
            log.info(
                "Deleted {} old completed profile operations created before {}",
                deletedOperations,
                createdBefore
            );
        }
    }
}
