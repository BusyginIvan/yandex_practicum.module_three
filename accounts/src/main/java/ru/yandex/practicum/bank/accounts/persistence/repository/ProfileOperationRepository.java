package ru.yandex.practicum.bank.accounts.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;

import java.util.List;

public interface ProfileOperationRepository extends JpaRepository<ProfileOperationEntity, String> {
    List<ProfileOperationEntity> findByStageOrderByCreatedAtAsc(
        ProfileOperationStage status,
        Pageable pageable
    );
}
