package ru.yandex.practicum.bank.transfers.api.model;

public record TransferRequest(
    String recipientLogin,
    int amount
) { }
