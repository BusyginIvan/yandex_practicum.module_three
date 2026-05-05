package ru.yandex.practicum.bank.accounts.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorCode;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorResponse;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationRequest;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.accounts.service.BalanceService;

@RestController
@RequestMapping("/accounts")
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/{login}/balance")
    public ResponseEntity<?> performBalanceOperation(
        @PathVariable String login,
        @RequestHeader("Operation-Id") String operationId,
        @RequestBody BalanceOperationRequest request
    ) {
        BalanceOperationResult result = balanceService.performBalanceOperation(
            login,
            operationId,
            request.type(),
            request.amount()
        );
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok().build();
            case INSUFFICIENT_FUNDS -> insufficientFundsResponse();
        };
    }

    private ResponseEntity<BalanceOperationErrorResponse> insufficientFundsResponse() {
        return ResponseEntity.badRequest().body(
            new BalanceOperationErrorResponse(BalanceOperationErrorCode.INSUFFICIENT_FUNDS));
    }
}
