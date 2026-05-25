package ru.yandex.practicum.bank.cash.api.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;

public record CashOperationRequest(
    @NotNull(message = "Cash operation type must not be null")
    BalanceOperationType type,

    @Positive(message = "Amount must be greater than zero")
    int amount
) { }

