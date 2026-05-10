package ru.yandex.practicum.bank.notifications.api.model;

public record TransferNotificationRequest(
    String senderLogin,
    String recipientLogin,
    int amount
) { }
