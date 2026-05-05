package ru.yandex.practicum.bank.accounts.domain;

public enum BalanceOperationResult {
    SUCCESS,
    INSUFFICIENT_FUNDS;

    public BalanceOperationStatus toStatus() { return switch (this) {
        case SUCCESS -> BalanceOperationStatus.SUCCESS;
        case INSUFFICIENT_FUNDS ->  BalanceOperationStatus.INSUFFICIENT_FUNDS;
    };}
}
