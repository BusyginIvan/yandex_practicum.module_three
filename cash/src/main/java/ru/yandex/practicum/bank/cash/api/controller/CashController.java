package ru.yandex.practicum.bank.cash.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorCode;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorResponse;
import ru.yandex.practicum.bank.cash.api.model.CashOperationRequest;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.service.CashService;

@RestController
@RequestMapping("/cash")
public class CashController {
    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cash:write')")
    public ResponseEntity<?> performCashOperation(@RequestBody CashOperationRequest request) {
        BalanceOperationResult result = cashService.performCashOperation(request.amount(), request.type());
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok().build();
            case INSUFFICIENT_FUNDS -> insufficientFundsResponse();
            case ERROR -> ResponseEntity.accepted().build();
        };
    }

    private ResponseEntity<CashOperationErrorResponse> insufficientFundsResponse() {
        return ResponseEntity.badRequest().body(
            new CashOperationErrorResponse(CashOperationErrorCode.INSUFFICIENT_FUNDS));
    }
}
