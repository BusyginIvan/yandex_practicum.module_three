package ru.yandex.practicum.bank.cash.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;

public interface CashOperationRepository extends JpaRepository<CashOperationEntity, String> { }
