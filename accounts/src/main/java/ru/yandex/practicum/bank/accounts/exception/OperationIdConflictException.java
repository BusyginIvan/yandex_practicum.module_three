package ru.yandex.practicum.bank.accounts.exception;

public class OperationIdConflictException extends RuntimeException {
    private final String operationId;

    public OperationIdConflictException(String operationId, String message) {
        super(message);
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }
}
