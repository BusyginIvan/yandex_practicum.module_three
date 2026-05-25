package ru.yandex.practicum.bank.accounts.api.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;

public record BalanceOperationRequest(
    @NotNull(message = "Balance operation type must not be null")
    BalanceOperationType type,

    @Positive(message = "Amount must be greater than zero")
    int amount
) { }
