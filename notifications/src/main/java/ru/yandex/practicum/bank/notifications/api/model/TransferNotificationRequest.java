package ru.yandex.practicum.bank.notifications.api.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferNotificationRequest(
    @Size(max = 255, message = "Login must not exceed 255 characters")
    String senderLogin,

    @Size(max = 255, message = "Login must not exceed 255 characters")
    String recipientLogin,

    @Positive(message = "Amount must be greater than zero")
    int amount
) { }
