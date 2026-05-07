package ru.yandex.practicum.bank.cash.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;

import java.time.Instant;
import java.util.List;

public interface CashOperationRepository extends JpaRepository<CashOperationEntity, String> {
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
}
