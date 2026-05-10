package ru.yandex.practicum.bank.accounts.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;

import java.time.Instant;

@Entity
@Table(name = "profile_operations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProfileOperationEntity {
    @Id
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @Column(name = "login", nullable = false)
    private String login;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ProfileOperationStage stage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProfileOperationEntity(
        String operationId,
        String login,
        ProfileOperationStage stage
    ) {
        this.operationId = operationId;
        this.login = login;
        this.stage = stage;
    }
}
