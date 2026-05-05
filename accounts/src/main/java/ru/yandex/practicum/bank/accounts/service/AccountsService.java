package ru.yandex.practicum.bank.accounts.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;
import ru.yandex.practicum.bank.accounts.persistence.entity.AccountEntity;
import ru.yandex.practicum.bank.accounts.persistence.repository.AccountRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountsService {
    private final AccountRepository accountRepository;
    private final CurrentAccountService currentAccountService;

    public AccountsService(
        AccountRepository accountRepository,
        CurrentAccountService currentAccountService
    ) {
        this.accountRepository = accountRepository;
        this.currentAccountService = currentAccountService;
    }

    public Account getCurrentAccount() {
        String login = currentAccountService.getCurrentLogin();
        return toAccount(accountRepository.findById(login)
            .orElseGet(() -> new AccountEntity(login, null, null, 0)));
    }

    @Transactional
    public Account updateCurrentAccount(String name, LocalDate birthdate) {
        String login = currentAccountService.getCurrentLogin();
        accountRepository.upsertProfile(login, name, birthdate);
        return toAccount(accountRepository.findById(login)
            .orElseThrow(() -> new IllegalStateException("Account not found after profile update")));
    }

    public List<AccountListItem> getAccounts() {
        return accountRepository.findAll().stream()
            .map(account -> new AccountListItem(account.getLogin(), account.getName()))
            .toList();
    }

    private Account toAccount(AccountEntity accountEntity) {
        return new Account(
            accountEntity.getLogin(),
            accountEntity.getName(),
            accountEntity.getBirthdate(),
            accountEntity.getBalance()
        );
    }
}
