package ru.yandex.practicum.bank.cash.integration.accounts.model;

import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;

public record AccountsBalanceOperationRequest(
    BalanceOperationType type,
    int amount
) { }
