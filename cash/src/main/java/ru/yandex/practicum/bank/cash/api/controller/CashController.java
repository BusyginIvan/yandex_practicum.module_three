package ru.yandex.practicum.bank.cash.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.cash.api.model.CashOperationRequest;

@RestController
@RequestMapping("/cash")
public class CashController {
    @PostMapping
    @PreAuthorize("hasAuthority('cash:write')")
    public ResponseEntity<?> performCashOperation(@RequestBody CashOperationRequest request) {
        return ResponseEntity.ok().build();
    }
}
