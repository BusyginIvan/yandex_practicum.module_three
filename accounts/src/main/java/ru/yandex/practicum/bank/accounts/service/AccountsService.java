package ru.yandex.practicum.bank.accounts.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountsService {
    private static final String CURRENT_LOGIN = "ivanov";
    private static final int CURRENT_BALANCE = 100;
    private static final List<AccountListItem> ACCOUNTS = List.of(
        new AccountListItem("petrov", "Петров Петр"),
        new AccountListItem("sidorov", "Сидоров Сидор")
    );

    private String currentName = "Иванов Иван";
    private LocalDate currentBirthdate = LocalDate.of(2001, 1, 1);

    public Account getCurrentAccount() {
        return new Account(CURRENT_LOGIN, currentName, currentBirthdate, CURRENT_BALANCE);
    }

    public Account updateCurrentAccount(String name, LocalDate birthdate) {
        currentName = name;
        currentBirthdate = birthdate;
        return getCurrentAccount();
    }

    public List<AccountListItem> getAccounts() {
        return ACCOUNTS;
    }
}
