package ru.yandex.practicum.bank.notifications.api.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.bank.notifications.domain.CashOperationType;

public record CashNotificationRequest(
    @Size(max = 255, message = "Login must not exceed 255 characters")
    String login,

    @NotNull(message = "Cash operation type must not be null")
    CashOperationType type,

    @Positive(message = "Amount must be greater than zero")
    int amount
) { }