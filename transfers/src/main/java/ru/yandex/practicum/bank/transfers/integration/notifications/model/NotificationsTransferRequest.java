package ru.yandex.practicum.bank.transfers.integration.notifications.model;

public record NotificationsTransferRequest(
    String senderLogin,
    String recipientLogin,
    int amount
) { }
