package ru.yandex.practicum.bank.ui.integration.cash.model;

import ru.yandex.practicum.bank.ui.domain.CashOperationType;

public record CashOperationRequest(
    CashOperationType type,
    int amount
) { }
