package ru.yandex.practicum.bank.cash.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;

import java.time.Instant;
import java.util.List;

public interface CashOperationRepository extends JpaRepository<CashOperationEntity, String> {
    List<CashOperationEntity> findByStageOrderByCreatedAtAsc(CashOperationStage stage, Pageable pageable);

    @Query("""
        select o
        from CashOperationEntity o
        where o.stage = :stage
          and o.createdAt <= :createdBefore
        order by o.createdAt asc
    """)
    List<CashOperationEntity> findByStageCreatedBefore(
        @Param("stage") CashOperationStage stage,
        @Param("createdBefore") Instant createdBefore,
        Pageable pageable
    );

    @Modifying
    @Transactional
    @Query(value = """
        delete from cash_operations
        where operation_id in (
            select operation_id
            from cash_operations
            where stage in ('COMPLETED', 'REJECTED_INSUFFICIENT_FUNDS')
              and created_at < :createdBefore
            order by created_at asc
            limit :batchSize
        )
    """, nativeQuery = true)
    int deleteFinalOperationsCreatedBefore(
        @Param("createdBefore") Instant createdBefore,
        @Param("batchSize") int batchSize
    );
}
