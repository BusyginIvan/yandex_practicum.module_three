package ru.yandex.practicum.bank.transfers.api.model;

public record TransferOperationErrorResponse(
    TransferOperationErrorCode code
) { }
