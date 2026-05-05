package ru.yandex.practicum.bank.accounts.api.model;

import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;

public record BalanceOperationRequest(
    BalanceOperationType type,
    int amount
) { }
