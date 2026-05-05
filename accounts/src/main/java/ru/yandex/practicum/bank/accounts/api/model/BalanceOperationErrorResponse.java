package ru.yandex.practicum.bank.accounts.api.model;

public record BalanceOperationErrorResponse(
    BalanceOperationErrorCode code,
    String message
) {
    public BalanceOperationErrorResponse(BalanceOperationErrorCode code) {
        this(code, null);
    }
}
