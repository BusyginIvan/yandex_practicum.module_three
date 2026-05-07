package ru.yandex.practicum.bank.ui.integration.transfers.model;

public record TransferRequest(
    String recipientLogin,
    int amount
) { }
