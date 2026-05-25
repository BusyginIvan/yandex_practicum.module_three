package ru.yandex.practicum.bank.accounts.api.model;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAccountRequest(
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    @Past(message = "Birthdate must be in the past")
    LocalDate birthdate
) { }
