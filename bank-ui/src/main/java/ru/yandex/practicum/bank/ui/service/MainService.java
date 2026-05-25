package ru.yandex.practicum.bank.ui.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.domain.MessageType;
import ru.yandex.practicum.bank.ui.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.ui.integration.cash.CashClient;
import ru.yandex.practicum.bank.ui.integration.transfers.TransfersClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class MainService {
    private final AccountsClient accountsClient;
    private final CashClient cashClient;
    private final TransfersClient transfersClient;
    private final CurrentAccountService currentAccountService;

    public MainService(
        AccountsClient accountsClient,
        CashClient cashClient,
        TransfersClient transfersClient,
        CurrentAccountService currentAccountService
    ) {
        this.accountsClient = accountsClient;
        this.cashClient = cashClient;
        this.transfersClient = transfersClient;
        this.currentAccountService = currentAccountService;
    }

    public void setNameAndBirthdate(Model model, String name, LocalDate birthdate) {
        AccountDetails profile = accountsClient.updateMe(name, birthdate);
        fillModel(model, profile, null, null);
    }

    public void performCashOperation(Model model, int amount, CashOperationType type) {
        BalanceOperationStatus status = cashClient.performCashOperation(amount, type);
        switch (status) {
            case SUCCESS -> fillModel(model,
                type == CashOperationType.DEPOSIT
                    ? "Положено %d руб".formatted(amount)
                    : "Снято %d руб".formatted(amount),
                MessageType.SUCCESS);
            case INSUFFICIENT_FUNDS -> fillModel(model, "Недостаточно средств на счету", MessageType.ERROR);
            case PROCESSING -> fillModel(model, "Операция в обработке", MessageType.PENDING);
            case ERROR -> fillModel(model, "Что-то пошло не так", MessageType.ERROR);
        }
    }

    public void transfer(Model model, int amount, String login) {
        BalanceOperationStatus status = transfersClient.transfer(amount, login);
        switch (status) {
            case SUCCESS -> fillModel(model,
                "Перевод на %d руб пользователю %s выполнен".formatted(amount, login),
                MessageType.SUCCESS);
            case INSUFFICIENT_FUNDS -> fillModel(model, "Недостаточно средств на счету", MessageType.ERROR);
            case PROCESSING -> fillModel(model, "Перевод в обработке", MessageType.PENDING);
            case ERROR -> fillModel(model, "Что-то пошло не так", MessageType.ERROR);
        }
    }

    public void fillModel(Model model, String message, MessageType messageType) {
        AccountDetails profile = accountsClient.getMe();
        fillModel(model, profile, message, messageType);
    }

    private void fillModel(Model model, AccountDetails profile, String message, MessageType messageType) {
        model.addAttribute("name", profile.nameOrEmpty());
        model.addAttribute("birthdate", profile.birthdateAsString());
        model.addAttribute("balance", profile.balance());
        model.addAttribute("accounts", getOtherAccounts());
        model.addAttribute("message", message);
        model.addAttribute("messageType", messageType);
    }

    private List<AccountListItem> getOtherAccounts() {
        String currentLogin = currentAccountService.getCurrentLogin();
        return accountsClient.getAccounts().stream()
            .filter(account -> account.name() != null)
            .filter(account -> !account.login().equals(currentLogin))
            .toList();
    }
}
