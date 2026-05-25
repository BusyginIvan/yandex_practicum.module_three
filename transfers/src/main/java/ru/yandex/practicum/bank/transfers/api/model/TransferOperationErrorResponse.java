package ru.yandex.practicum.bank.transfers.api.model;

public record TransferOperationErrorResponse(
    TransferOperationErrorCode code,
    String message
) {
    public TransferOperationErrorResponse(TransferOperationErrorCode code) {
        this(code, null);
    }
}
