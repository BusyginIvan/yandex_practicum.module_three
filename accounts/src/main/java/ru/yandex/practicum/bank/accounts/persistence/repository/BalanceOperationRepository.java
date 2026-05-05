package ru.yandex.practicum.bank.accounts.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;
import ru.yandex.practicum.bank.accounts.persistence.entity.BalanceOperationEntity;

public interface BalanceOperationRepository extends JpaRepository<BalanceOperationEntity, String> {
    default int createIfMissing(
        String operationId,
        String login,
        BalanceOperationType type,
        int amount,
        BalanceOperationStatus status
    ) {
        return createIfMissingRaw(
            operationId,
            login,
            type.name(),
            amount,
            status.name()
        );
    }

    @Modifying
    @Query(value = """
        insert into balance_operations(operation_id, login, type, amount, status)
        values (:operationId, :login, :type, :amount, :status)
        on conflict (operation_id) do nothing
        """, nativeQuery = true)
    int createIfMissingRaw(
        @Param("operationId") String operationId,
        @Param("login") String login,
        @Param("type") String type,
        @Param("amount") int amount,
        @Param("status") String status
    );

    default int updateStatus(String operationId, BalanceOperationStatus status) {
        return updateStatusRaw(operationId, status.name());
    }

    @Modifying
    @Query(value = """
        update balance_operations
        set status = :status
        where operation_id = :operationId
        """, nativeQuery = true)
    int updateStatusRaw(
        @Param("operationId") String operationId,
        @Param("status") String status
    );
}
