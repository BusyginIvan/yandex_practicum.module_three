package ru.yandex.practicum.bank.notifications.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.notifications.persistence.entity.ProcessedNotificationEntity;

import java.time.Instant;

public interface ProcessedNotificationRepository extends JpaRepository<ProcessedNotificationEntity, String> {
    @Modifying
    @Transactional
    @Query(value = """
        insert into processed_notifications(operation_id)
        values (:operationId)
        on conflict do nothing
    """, nativeQuery = true)
    int createIfMissing(@Param("operationId") String operationId);

    @Modifying
    @Transactional
    @Query(value = """
        delete from processed_notifications
        where operation_id in (
            select operation_id
            from processed_notifications
            where created_at < :createdBefore
            order by created_at asc
            limit :batchSize
        )
    """, nativeQuery = true)
    int deleteCreatedBefore(
        @Param("createdBefore") Instant createdBefore,
        @Param("batchSize") int batchSize
    );
}
