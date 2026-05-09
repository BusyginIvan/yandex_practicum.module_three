package ru.yandex.practicum.bank.notifications.api.model;

import ru.yandex.practicum.bank.notifications.domain.CashOperationType;

public record CashNotificationRequest(
    String login,
    CashOperationType type,
    int amount
) { }
