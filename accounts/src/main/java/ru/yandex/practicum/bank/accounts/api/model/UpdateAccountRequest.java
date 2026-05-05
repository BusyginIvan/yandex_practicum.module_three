package ru.yandex.practicum.bank.accounts.api.model;

import java.time.LocalDate;

public record UpdateAccountRequest(
    String name,
    LocalDate birthdate
) { }
