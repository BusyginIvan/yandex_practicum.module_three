package ru.yandex.practicum.bank.accounts.api.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.accounts.api.controller.BalanceController;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorCode;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorResponse;
import ru.yandex.practicum.bank.accounts.exception.OperationIdConflictException;

@RestControllerAdvice(assignableTypes = BalanceController.class)
public class BalanceExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BalanceExceptionHandler.class);

    @ExceptionHandler(OperationIdConflictException.class)
    public ResponseEntity<BalanceOperationErrorResponse> handleOperationIdConflict(
        OperationIdConflictException ex
    ) {
        log.warn(
            "Rejected balance operation request, operationId={}: {}",
            ex.getOperationId(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest()
            .body(new BalanceOperationErrorResponse(BalanceOperationErrorCode.BAD_REQUEST));
    }
}
