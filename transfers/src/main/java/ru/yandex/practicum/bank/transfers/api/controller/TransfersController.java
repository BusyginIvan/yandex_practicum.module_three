package ru.yandex.practicum.bank.transfers.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.transfers.api.model.TransferRequest;

@RestController
@RequestMapping("/transfers")
public class TransfersController {
    @PostMapping
    @PreAuthorize("hasAuthority('cash:write')")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok().build();
    }
}
