package ru.yandex.practicum.bank.ui.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.domain.MessageType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MainService {
    private String name = "Иванов Иван";
    private LocalDate birthdate = LocalDate.of(2001, 1, 1);
    private int balance = 100;

    private final List<AccountListItem> accounts = List.of(
        new AccountListItem("petrov", "Петров Петр"),
        new AccountListItem("sidorov", "Сидоров Сидор")
    );

    public void setNameAndBirthdate(String name, LocalDate birthdate) {
        this.name = name;
        this.birthdate = birthdate;
    }

    public void performCashOperation(Model model, int amount, CashOperationType type) {
        if (type == CashOperationType.DEPOSIT) {
            balance = balance + amount;
            fillModel(model, "Положено %d руб".formatted(amount), MessageType.SUCCESS);
        } else if (balance >= amount) {
            balance = balance - amount;
            fillModel(model, "Снято %d руб".formatted(amount), MessageType.SUCCESS);
        } else {
            fillModel(model, "Недостаточно средств на счету", MessageType.ERROR);
        }
    }

    public void transfer(Model model, int amount, String login) {
        if (balance >= amount) {
            balance = balance - amount;
            fillModel(model, "Успешно переведено %d руб клиенту %s".formatted(amount, login), MessageType.SUCCESS);
        } else {
            fillModel(model, "Недостаточно средств на счету", MessageType.ERROR);
        }
    }

    public void fillModel(Model model, String message, MessageType messageType) {
        model.addAttribute("name", name);
        model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("balance", balance);
        model.addAttribute("accounts", accounts);
        model.addAttribute("message", message);
        model.addAttribute("messageType", messageType);
    }
}
