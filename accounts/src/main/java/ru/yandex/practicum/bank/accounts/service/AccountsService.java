package ru.yandex.practicum.bank.accounts.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.integration.notifications.NotificationsClient;
import ru.yandex.practicum.bank.accounts.persistence.entity.AccountEntity;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;
import ru.yandex.practicum.bank.accounts.persistence.repository.AccountRepository;
import ru.yandex.practicum.bank.accounts.persistence.repository.ProfileOperationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AccountsService {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final AccountRepository accountRepository;
    private final ProfileOperationRepository profileOperationRepository;
    private final NotificationsClient notificationsClient;
    private final CurrentAccountService currentAccountService;

    public AccountsService(
        AccountRepository accountRepository,
        ProfileOperationRepository profileOperationRepository,
        NotificationsClient notificationsClient,
        CurrentAccountService currentAccountService
    ) {
        this.accountRepository = accountRepository;
        this.profileOperationRepository = profileOperationRepository;
        this.notificationsClient = notificationsClient;
        this.currentAccountService = currentAccountService;
    }

    public Account getCurrentAccount() {
        String login = currentAccountService.getCurrentLogin();
        return toAccount(accountRepository.findById(login)
            .orElseGet(() -> new AccountEntity(login, null, null, 0)));
    }

    @Transactional
    public Account updateCurrentAccount(String name, LocalDate birthdate) {
        String login = currentAccountService.getCurrentLogin();
        accountRepository.upsertProfile(login, name, birthdate);
        profileOperationRepository.save(new ProfileOperationEntity(
            UUID.randomUUID().toString(),
            login,
            ProfileOperationStage.NOTIFICATION_PENDING
        ));
        return toAccount(accountRepository.findById(login)
            .orElseThrow(() -> new IllegalStateException("Account not found after profile update")));
    }

    public void sendProfileUpdateNotification(ProfileOperationEntity operation) {
        if (operation.getStage() != ProfileOperationStage.NOTIFICATION_PENDING) {
            throw new IllegalStateException(
                "Cannot send profile update notification: unexpected stage '%s' for operationId=%s"
                    .formatted(operation.getStage(), operation.getOperationId())
            );
        }

        if (notificationsClient.sendProfileUpdateNotification(
            operation.getOperationId(),
            operation.getLogin()
        )) {
            try {
                operation.setStage(ProfileOperationStage.COMPLETED);
                profileOperationRepository.save(operation);
            } catch (Exception ex) {
                log.error("Failed to persist profile operation stage, operationId={}", operation.getOperationId(), ex);
            }
        }
    }

    public List<AccountListItem> getAccounts() {
        return accountRepository.findAll().stream()
            .map(account -> new AccountListItem(account.getLogin(), account.getName()))
            .toList();
    }

    private Account toAccount(AccountEntity accountEntity) {
        return new Account(
            accountEntity.getLogin(),
            accountEntity.getName(),
            accountEntity.getBirthdate(),
            accountEntity.getBalance()
        );
    }
}
