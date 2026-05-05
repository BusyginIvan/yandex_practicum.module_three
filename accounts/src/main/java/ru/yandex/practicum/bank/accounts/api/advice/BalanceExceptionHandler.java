package ru.yandex.practicum.bank.accounts.api.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.yandex.practicum.bank.accounts.api.controller.BalanceController;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorCode;
import ru.yandex.practicum.bank.accounts.api.model.BalanceOperationErrorResponse;
import ru.yandex.practicum.bank.accounts.exception.OperationIdConflictException;

@RestControllerAdvice(assignableTypes = BalanceController.class)
public class BalanceExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BalanceExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BalanceOperationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation failed");
        return ResponseEntity.badRequest()
            .body(new BalanceOperationErrorResponse(BalanceOperationErrorCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<BalanceOperationErrorResponse> handleMethodValidationException(
        HandlerMethodValidationException ex
    ) {
        String message = ex.getAllErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable.class::cast)
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation failed");
        return ResponseEntity.badRequest()
            .body(new BalanceOperationErrorResponse(BalanceOperationErrorCode.BAD_REQUEST, message));
    }

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
