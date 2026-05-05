package ru.yandex.practicum.bank.ui.integration.accounts.model;

import java.time.LocalDate;

public record UpdateAccountRequest(
    String name,
    LocalDate birthdate
) { }
