package ru.yandex.practicum.bank.cash.api.advice;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorCode;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorResponse;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CashOperationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation failed");
        return ResponseEntity.badRequest()
            .body(new CashOperationErrorResponse(CashOperationErrorCode.BAD_REQUEST, message));
    }
}
