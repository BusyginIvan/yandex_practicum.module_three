package ru.yandex.practicum.bank.transfers.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface TransferOperationRepository extends JpaRepository<TransferOperationEntity, String> {
    @Query("""
        select o
        from TransferOperationEntity o
        where o.stage in :stages
          and o.createdAt <= :createdBefore
        order by o.createdAt asc
    """)
    List<TransferOperationEntity> findByStageInCreatedBefore(
        @Param("stages") Collection<TransferOperationStage> stages,
        @Param("createdBefore") Instant createdBefore,
        Pageable pageable
    );

    @Modifying
    @Transactional
    @Query(value = """
        delete from transfer_operations
        where operation_id in (
            select operation_id
            from transfer_operations
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
