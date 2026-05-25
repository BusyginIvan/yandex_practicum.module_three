package ru.yandex.practicum.bank.ui.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AccountDetails(
    String login,
    String name,
    LocalDate birthdate,
    int balance
) {
    public String nameOrEmpty() {
        return Objects.requireNonNullElse(name, "");
    }

    public String birthdateAsString() {
        return birthdate == null ? "" : birthdate.format(DateTimeFormatter.ISO_DATE);
    }
}
