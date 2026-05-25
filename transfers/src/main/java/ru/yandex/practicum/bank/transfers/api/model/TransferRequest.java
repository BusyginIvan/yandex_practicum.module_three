package ru.yandex.practicum.bank.transfers.api.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferRequest(
    @Size(max = 255, message = "Login must not exceed 255 characters")
    String recipientLogin,

    @Positive(message = "Amount must be greater than zero")
    int amount
) { }
