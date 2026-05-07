package ru.yandex.practicum.bank.accounts.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.accounts.api.model.UpdateAccountRequest;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;
import ru.yandex.practicum.bank.accounts.service.AccountsService;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountsController {
    private final AccountsService accountsService;

    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('accounts:read')")
    public Account getMe() {
        return accountsService.getCurrentAccount();
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('accounts:write')")
    public Account updateMe(@RequestBody UpdateAccountRequest request) {
        return accountsService.updateCurrentAccount(request.name(), request.birthdate());
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('accounts:read')")
    public List<AccountListItem> getList() {
        return accountsService.getAccounts();
    }
}
