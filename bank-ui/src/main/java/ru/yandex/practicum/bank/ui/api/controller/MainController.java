package ru.yandex.practicum.bank.ui.api.controller;

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
        @RequestParam("name") String name,
        @RequestParam("birthdate") LocalDate birthdate
    ) {
        mainService.setNameAndBirthdate(model, name, birthdate);
        return "main";
    }

    @PostMapping("/cash")
    public String performCashOperation(
        Model model,
        @RequestParam("amount") int amount,
        @RequestParam("action") CashOperationType action
    ) {
        mainService.performCashOperation(model, amount, action);
        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
        Model model,
        @RequestParam("amount") int amount,
        @RequestParam("login") String login
    ) {
        mainService.transfer(model, amount, login);
        return "main";
    }
}
