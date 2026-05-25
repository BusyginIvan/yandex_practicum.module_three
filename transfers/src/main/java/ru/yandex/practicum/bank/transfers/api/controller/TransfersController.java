package ru.yandex.practicum.bank.transfers.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.transfers.api.model.TransferOperationErrorCode;
import ru.yandex.practicum.bank.transfers.api.model.TransferOperationErrorResponse;
import ru.yandex.practicum.bank.transfers.api.model.TransferRequest;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationResult;
import ru.yandex.practicum.bank.transfers.service.TransferService;

@RestController
@RequestMapping("/transfers")
public class TransfersController {
    private final TransferService transferService;

    public TransfersController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('transfers:write')")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        TransferOperationResult result = transferService.performTransfer(
            request.amount(),
            request.recipientLogin()
        );
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok().build();
            case INSUFFICIENT_FUNDS -> insufficientFundsResponse();
            case ERROR -> ResponseEntity.accepted().build();
        };
    }

    private ResponseEntity<TransferOperationErrorResponse> insufficientFundsResponse() {
        return ResponseEntity.badRequest().body(
            new TransferOperationErrorResponse(TransferOperationErrorCode.INSUFFICIENT_FUNDS)
        );
    }
}
