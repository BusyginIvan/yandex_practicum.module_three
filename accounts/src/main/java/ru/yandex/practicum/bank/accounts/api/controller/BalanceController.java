package ru.yandex.practicum.bank.accounts.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('accounts:balance:write')")
    public ResponseEntity<?> performBalanceOperation(
        @PathVariable
        @Size(max = 255, message = "Login must not exceed 255 characters")
        String login,

        @RequestHeader("Operation-Id")
        @Size(max = 255, message = "Operation-Id must not exceed 255 characters")
        String operationId,

        @Valid
        @RequestBody
        BalanceOperationRequest request
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
