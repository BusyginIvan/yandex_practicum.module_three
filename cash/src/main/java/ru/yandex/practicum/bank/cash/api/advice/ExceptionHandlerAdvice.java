package ru.yandex.practicum.bank.cash.api.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorCode;
import ru.yandex.practicum.bank.cash.api.model.CashOperationErrorResponse;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CashOperationErrorResponse> handleException(Exception ex) {
        if (ex instanceof AuthenticationException authenticationException) {
            throw authenticationException;
        }

        log.error("Unhandled exception in cash API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new CashOperationErrorResponse(CashOperationErrorCode.SERVER_ERROR));
    }
}
