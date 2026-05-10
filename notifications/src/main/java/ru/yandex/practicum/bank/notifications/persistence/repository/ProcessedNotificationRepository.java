package ru.yandex.practicum.bank.notifications.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.notifications.persistence.entity.ProcessedNotificationEntity;

public interface ProcessedNotificationRepository extends JpaRepository<ProcessedNotificationEntity, String> {
    @Modifying
    @Transactional
    @Query(value = """
        insert into processed_notifications(operation_id)
        values (:operationId)
        on conflict do nothing
    """, nativeQuery = true)
    int createIfMissing(@Param("operationId") String operationId);
}
