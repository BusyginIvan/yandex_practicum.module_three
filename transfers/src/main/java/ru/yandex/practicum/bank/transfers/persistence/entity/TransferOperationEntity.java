package ru.yandex.practicum.bank.transfers.persistence.entity;

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
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;

import java.time.Instant;

@Entity
@Table(name = "transfer_operations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TransferOperationEntity {
    @Id
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @Column(name = "sender_login", nullable = false)
    private String senderLogin;

    @Column(name = "recipient_login", nullable = false)
    private String recipientLogin;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "withdraw_operation_id", nullable = false, unique = true)
    private String withdrawOperationId;

    @Column(name = "deposit_operation_id", nullable = false, unique = true)
    private String depositOperationId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private TransferOperationStage stage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TransferOperationEntity(
        String operationId,
        String senderLogin,
        String recipientLogin,
        int amount,
        String withdrawOperationId,
        String depositOperationId,
        TransferOperationStage stage
    ) {
        this.operationId = operationId;
        this.senderLogin = senderLogin;
        this.recipientLogin = recipientLogin;
        this.amount = amount;
        this.withdrawOperationId = withdrawOperationId;
        this.depositOperationId = depositOperationId;
        this.stage = stage;
    }
}
