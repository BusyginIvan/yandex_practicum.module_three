package ru.yandex.practicum.bank.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.cash.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.cash.persistence.entity.CashOperationEntity;
import ru.yandex.practicum.bank.cash.persistence.repository.CashOperationRepository;

import java.util.UUID;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final CashOperationRepository cashOperationRepository;
    private final CurrentAccountService currentAccountService;

    public CashService(
        AccountsClient accountsClient,
        NotificationsClient notificationsClient,
        CashOperationRepository cashOperationRepository,
        CurrentAccountService currentAccountService
    ) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.cashOperationRepository = cashOperationRepository;
        this.currentAccountService = currentAccountService;
    }

    public BalanceOperationResult performCashOperation(int amount, BalanceOperationType type) {
        String login = currentAccountService.getCurrentLogin();
        String operationId = UUID.randomUUID().toString();

        CashOperationEntity operation = new CashOperationEntity(
            operationId,
            login,
            type,
            amount,
            CashOperationStage.NEW
        );
        cashOperationRepository.save(operation);

        return processOperation(operation);
    }

    public BalanceOperationResult processOperation(CashOperationEntity operation) {
        if (operation.getStage() != CashOperationStage.NEW)
            throw new IllegalStateException(
                "Cannot process cash operation: unexpected stage '%s' for operationId=%s"
                    .formatted(operation.getStage(), operation.getOperationId())
            );

        BalanceOperationResult result = accountsClient.performBalanceOperation(
            operation.getOperationId(),
            operation.getLogin(),
            operation.getAmount(),
            operation.getType()
        );
        try {
            if (result == BalanceOperationResult.SUCCESS) {
                operation.setStage(CashOperationStage.NOTIFICATION_PENDING);
                cashOperationRepository.save(operation);
            } else if (result == BalanceOperationResult.INSUFFICIENT_FUNDS) {
                operation.setStage(CashOperationStage.REJECTED_INSUFFICIENT_FUNDS);
                cashOperationRepository.save(operation);
            }
        } catch (Exception ex) {
            log.error("Failed to persist cash operation state, operationId={}", operation.getOperationId(), ex);
            return BalanceOperationResult.ERROR;
        }
        return result;
    }

    public boolean sendNotification(CashOperationEntity operation) {
        if (operation.getStage() != CashOperationStage.NOTIFICATION_PENDING)
            throw new IllegalStateException(
                "Cannot send notification for cash operation: unexpected stage '%s' for operationId=%s"
                    .formatted(operation.getStage(), operation.getOperationId())
            );

        if (!notificationsClient.sendCashNotification(
            operation.getOperationId(),
            operation.getLogin(),
            operation.getAmount(),
            operation.getType()
        )) return false;

        try {
            operation.setStage(CashOperationStage.COMPLETED);
            cashOperationRepository.save(operation);
            return true;
        } catch (Exception ex) {
            log.error("Failed to persist cash operation state, operationId={}", operation.getOperationId(), ex);
            return false;
        }
    }
}
