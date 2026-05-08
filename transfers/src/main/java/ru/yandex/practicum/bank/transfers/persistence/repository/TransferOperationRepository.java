package ru.yandex.practicum.bank.transfers.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;

public interface TransferOperationRepository extends JpaRepository<TransferOperationEntity, String> {
}
