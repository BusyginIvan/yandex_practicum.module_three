package ru.yandex.practicum.bank.ui.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.domain.MessageType;
import ru.yandex.practicum.bank.ui.integration.accounts.AccountsClient;

import java.time.LocalDate;

@Service
public class MainService {
    private final AccountsClient accountsClient;

    public MainService(AccountsClient accountsClient) {
        this.accountsClient = accountsClient;
    }

    public void setNameAndBirthdate(Model model, String name, LocalDate birthdate) {
        AccountDetails profile = accountsClient.updateMe(name, birthdate);
        fillModel(model, profile, null, null);
    }

    public void performCashOperation(Model model, int amount, CashOperationType type) {
        fillModel(model, "Операции с наличными пока не поддерживаются", MessageType.ERROR);
    }

    public void transfer(Model model, int amount, String login) {
        fillModel(model, "Переводы пока не поддерживаются", MessageType.ERROR);
    }

    public void fillModel(Model model, String message, MessageType messageType) {
        AccountDetails profile = accountsClient.getMe();
        fillModel(model, profile, message, messageType);
    }

    private void fillModel(Model model, AccountDetails profile, String message, MessageType messageType) {
        model.addAttribute("name", profile.nameOrEmpty());
        model.addAttribute("birthdate", profile.birthdateAsString());
        model.addAttribute("balance", profile.balance());
        model.addAttribute("accounts", accountsClient.getAccounts());
        model.addAttribute("message", message);
        model.addAttribute("messageType", messageType);
    }
}
