package ru.yandex.practicum.bank.ui.api.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.service.MainService;

import java.time.LocalDate;

@Controller
public class MainController {
    @Autowired
    private MainService mainService;

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model) {
        mainService.fillModel(model, null, null);
        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
        Model model,

        @RequestParam("name")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @RequestParam("birthdate")
        @Past(message = "Birthdate must be in the past")
        LocalDate birthdate
    ) {
        mainService.setNameAndBirthdate(model, name, birthdate);
        return "main";
    }

    @PostMapping("/cash")
    public String performCashOperation(
        Model model,

        @RequestParam("amount")
        @Positive(message = "Amount must be greater than zero")
        int amount,

        @RequestParam("action")
        @NotNull(message = "Action must not be null")
        CashOperationType action
    ) {
        mainService.performCashOperation(model, amount, action);
        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
        Model model,

        @RequestParam("amount")
        @Positive(message = "Amount must be greater than zero")
        int amount,

        @RequestParam("login")
        @Size(max = 255, message = "Login must not exceed 255 characters")
        String login
    ) {
        mainService.transfer(model, amount, login);
        return "main";
    }
}
