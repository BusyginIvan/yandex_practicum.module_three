package ru.yandex.practicum.bank.transfers.domain;

public enum TransferOperationStage {
    NEW,
    WITHDRAW_SUCCEEDED,
    REJECTED_INSUFFICIENT_FUNDS,
    COMPLETED
}
