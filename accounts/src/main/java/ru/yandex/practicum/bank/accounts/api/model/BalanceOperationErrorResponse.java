package ru.yandex.practicum.bank.accounts.api.model;

public record BalanceOperationErrorResponse(
    BalanceOperationErrorCode code
) { }
