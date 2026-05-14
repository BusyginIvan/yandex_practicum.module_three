package ru.yandex.practicum.bank.accounts.persistence.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;

import java.time.Instant;
import java.util.List;

public interface ProfileOperationRepository extends JpaRepository<ProfileOperationEntity, String> {
    List<ProfileOperationEntity> findByStageOrderByCreatedAtAsc(
        ProfileOperationStage status,
        Pageable pageable
    );

    @Modifying
    @Transactional
    @Query(value = """
        delete from profile_operations
        where operation_id in (
            select operation_id
            from profile_operations
            where stage = 'COMPLETED'
              and created_at < :createdBefore
            order by created_at asc
            limit :batchSize
        )
    """, nativeQuery = true)
    int deleteCompletedOperationsCreatedBefore(
        @Param("createdBefore") Instant createdBefore,
        @Param("batchSize") int batchSize
    );
}
