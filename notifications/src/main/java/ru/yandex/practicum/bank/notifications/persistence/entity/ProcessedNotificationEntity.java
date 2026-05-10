package ru.yandex.practicum.bank.notifications.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "processed_notifications")
public class ProcessedNotificationEntity {
    @Id
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProcessedNotificationEntity() { }
}
