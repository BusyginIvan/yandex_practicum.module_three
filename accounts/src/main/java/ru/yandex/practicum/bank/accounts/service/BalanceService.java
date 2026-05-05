package ru.yandex.practicum.bank.accounts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;
import ru.yandex.practicum.bank.accounts.exception.OperationIdConflictException;
import ru.yandex.practicum.bank.accounts.persistence.entity.BalanceOperationEntity;
import ru.yandex.practicum.bank.accounts.persistence.repository.AccountRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.BalanceOperationRepository;

@Service
public class BalanceService {
    private final AccountRepository accountRepository;
    private final BalanceOperationRepository balanceOperationRepository;

    public BalanceService(
        AccountRepository accountRepository,
        BalanceOperationRepository balanceOperationRepository
    ) {
        this.accountRepository = accountRepository;
        this.balanceOperationRepository = balanceOperationRepository;
    }

    @Transactional
    public BalanceOperationResult performBalanceOperation(
        String login,
        String operationId,
        BalanceOperationType type,
        int amount
    ) {
        accountRepository.createIfMissing(login);

        int insertedRows = balanceOperationRepository.createIfMissing(
            operationId,
            login,
            type,
            amount,
            BalanceOperationStatus.PROCESSING
        );
        if (insertedRows == 0) {
            return getExistingOperationResult(login, operationId, type, amount);
        }

        BalanceOperationResult result = switch (type) {
            case DEPOSIT -> deposit(login, amount);
            case WITHDRAW -> withdraw(login, amount);
        };
        finalizeOperation(operationId, result);

        return result;
    }

    private BalanceOperationResult deposit(String login, int amount) {
        int updatedRows = accountRepository.deposit(login, amount);
        if (updatedRows != 1) {
            throw new IllegalStateException("Failed to deposit into account: " + login);
        }
        return BalanceOperationResult.SUCCESS;
    }

    private BalanceOperationResult withdraw(String login, int amount) {
        int updatedRows = accountRepository.withdrawIfEnough(login, amount);
        return updatedRows == 1
            ? BalanceOperationResult.SUCCESS
            : BalanceOperationResult.INSUFFICIENT_FUNDS;
    }

    private BalanceOperationResult getExistingOperationResult(
        String login,
        String operationId,
        BalanceOperationType type,
        int amount
    ) {
        BalanceOperationEntity existingOperation = balanceOperationRepository.findById(operationId)
            .orElseThrow(() -> new IllegalStateException("Operation exists but cannot be loaded: " + operationId));

        validateExistingOperation(existingOperation, login, type, amount);

        return switch (existingOperation.getStatus()) {
            case SUCCESS -> BalanceOperationResult.SUCCESS;
            case INSUFFICIENT_FUNDS ->  BalanceOperationResult.INSUFFICIENT_FUNDS;
            case PROCESSING -> throw new IllegalStateException(
                "Operation is still processing after conflict resolution: " + operationId
            );
        };
    }

    private void validateExistingOperation(
        BalanceOperationEntity existingOperation,
        String login,
        BalanceOperationType type,
        int amount
    ) {
        boolean sameRequest = existingOperation.getLogin().equals(login)
            && existingOperation.getType() == type
            && existingOperation.getAmount() == amount;
        if (!sameRequest) {
            throw new OperationIdConflictException(
                existingOperation.getOperationId(),
                "Operation-Id is already used for another request"
            );
        }
    }

    private void finalizeOperation(String operationId, BalanceOperationResult result) {
        int updatedRows = balanceOperationRepository.updateStatus(operationId, result.toStatus());
        if (updatedRows != 1) {
            throw new IllegalStateException("Failed to finalize operation: " + operationId);
        }
    }
}
