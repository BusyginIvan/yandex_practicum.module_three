package ru.yandex.practicum.bank.notifications.api.model;

import jakarta.validation.constraints.Size;

public record ProfileNotificationRequest(
    @Size(max = 255, message = "Login must not exceed 255 characters")
    String login
) { }
