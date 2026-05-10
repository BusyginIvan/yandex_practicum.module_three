package ru.yandex.practicum.bank.transfers.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationResult;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.transfers.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.transfers.persistence.entity.TransferOperationEntity;
import ru.yandex.practicum.bank.transfers.persistence.repository.TransferOperationRepository;

import java.util.UUID;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final TransferOperationRepository transferOperationRepository;
    private final CurrentAccountService currentAccountService;

    public TransferService(
        AccountsClient accountsClient,
        NotificationsClient notificationsClient,
        TransferOperationRepository transferOperationRepository,
        CurrentAccountService currentAccountService
    ) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.transferOperationRepository = transferOperationRepository;
        this.currentAccountService = currentAccountService;
    }

    public TransferOperationResult performTransfer(int amount, String recipientLogin) {
        String senderLogin = currentAccountService.getCurrentLogin();
        String operationId = UUID.randomUUID().toString();

        TransferOperationEntity operation = new TransferOperationEntity(
            operationId,
            senderLogin,
            recipientLogin,
            amount,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            TransferOperationStage.NEW
        );
        transferOperationRepository.save(operation);

        return processOperation(operation);
    }

    public TransferOperationResult processOperation(TransferOperationEntity operation) {
        if (operation.getStage() == TransferOperationStage.NEW) {
            BalanceOperationResult withdrawResult = accountsClient.performBalanceOperation(
                operation.getWithdrawOperationId(),
                operation.getSenderLogin(),
                operation.getAmount(),
                BalanceOperationType.WITHDRAW
            );
            if (withdrawResult == BalanceOperationResult.ERROR) {
                return TransferOperationResult.ERROR;
            }
            try {
                if (withdrawResult == BalanceOperationResult.INSUFFICIENT_FUNDS) {
                    operation.setStage(TransferOperationStage.REJECTED_INSUFFICIENT_FUNDS);
                    transferOperationRepository.save(operation);
                    return TransferOperationResult.INSUFFICIENT_FUNDS;
                } else {
                    operation.setStage(TransferOperationStage.WITHDRAW_SUCCEEDED);
                    transferOperationRepository.save(operation);
                }
            } catch (Exception ex) {
                log.error("Failed to persist withdraw stage, operationId={}", operation.getOperationId(), ex);
                return TransferOperationResult.ERROR;
            }
        }

        if (operation.getStage() == TransferOperationStage.WITHDRAW_SUCCEEDED) {
            BalanceOperationResult depositResult = accountsClient.performBalanceOperation(
                operation.getDepositOperationId(),
                operation.getRecipientLogin(),
                operation.getAmount(),
                BalanceOperationType.DEPOSIT
            );
            if (depositResult != BalanceOperationResult.SUCCESS) {
                return TransferOperationResult.ERROR;
            }

            try {
                operation.setStage(TransferOperationStage.NOTIFICATION_PENDING);
                transferOperationRepository.save(operation);
                return TransferOperationResult.SUCCESS;
            } catch (Exception ex) {
                log.error("Failed to persist transfer stage, operationId={}", operation.getOperationId(), ex);
                return TransferOperationResult.ERROR;
            }
        }

        throw new IllegalStateException(
            "Cannot process transfer operation: unexpected stage '%s' for operationId=%s"
                .formatted(operation.getStage(), operation.getOperationId())
        );
    }

    public void sendNotification(TransferOperationEntity operation) {
        if (operation.getStage() != TransferOperationStage.NOTIFICATION_PENDING) {
            throw new IllegalStateException(
                "Cannot send notification for transfer operation: unexpected stage '%s' for operationId=%s"
                    .formatted(operation.getStage(), operation.getOperationId())
            );
        }

        if (notificationsClient.sendTransferNotification(
            operation.getOperationId(),
            operation.getSenderLogin(),
            operation.getRecipientLogin(),
            operation.getAmount()
        )) {
            try {
                operation.setStage(TransferOperationStage.COMPLETED);
                transferOperationRepository.save(operation);
            } catch (Exception ex) {
                log.error("Failed to persist transfer stage, operationId={}", operation.getOperationId(), ex);
            }
        }
    }
}
