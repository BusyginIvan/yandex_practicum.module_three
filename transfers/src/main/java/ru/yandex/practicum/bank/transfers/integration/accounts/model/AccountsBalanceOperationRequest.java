package ru.yandex.practicum.bank.transfers.integration.accounts.model;

import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;

public record AccountsBalanceOperationRequest(
    BalanceOperationType type,
    int amount
) { }
