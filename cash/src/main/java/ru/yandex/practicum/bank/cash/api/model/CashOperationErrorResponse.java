package ru.yandex.practicum.bank.cash.api.model;

public record CashOperationErrorResponse(
    CashOperationErrorCode code,
    String message
) {
    public CashOperationErrorResponse(CashOperationErrorCode code) {
        this(code, null);
    }
}
