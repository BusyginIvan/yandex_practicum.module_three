package ru.yandex.practicum.bank.accounts.domain;

import java.time.LocalDate;

public record Account(
    String login,
    String name,
    LocalDate birthdate,
    int balance
) { }
