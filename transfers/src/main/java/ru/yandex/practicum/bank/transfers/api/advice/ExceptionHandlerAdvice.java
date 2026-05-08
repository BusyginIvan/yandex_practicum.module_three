package ru.yandex.practicum.bank.transfers.api.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.transfers.api.model.TransferOperationErrorCode;
import ru.yandex.practicum.bank.transfers.api.model.TransferOperationErrorResponse;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TransferOperationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation failed");
        return ResponseEntity.badRequest()
            .body(new TransferOperationErrorResponse(TransferOperationErrorCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TransferOperationErrorResponse> handleException(Exception ex) {
        if (ex instanceof AuthenticationException authenticationException) {
            throw authenticationException;
        }

        log.error("Unhandled exception in transfers API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new TransferOperationErrorResponse(TransferOperationErrorCode.SERVER_ERROR));
    }
}
