package ru.yandex.practicum.bank.accounts.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;
import ru.yandex.practicum.bank.accounts.domain.ProfileOperationStage;
import ru.yandex.practicum.bank.accounts.persistence.entity.AccountEntity;
import ru.yandex.practicum.bank.accounts.persistence.entity.ProfileOperationEntity;
import ru.yandex.practicum.bank.accounts.support.AbstractBusinessLayerTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountsServiceTest extends AbstractBusinessLayerTest {
    @Autowired
    private AccountsService accountsService;

    @Test
    void getCurrentAccountReturnsExistingAccount() {
        LocalDate birthdate = LocalDate.of(1990, 1, 2);

        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountRepository.findById("alice")).thenReturn(Optional.of(
            new AccountEntity("alice", "Alice", birthdate, 150)
        ));

        Account account = accountsService.getCurrentAccount();

        assertThat(account).isEqualTo(new Account("alice", "Alice", birthdate, 150));
    }

    @Test
    void getCurrentAccountReturnsDefaultAccountWhenMissing() {
        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountRepository.findById("alice")).thenReturn(Optional.empty());

        Account account = accountsService.getCurrentAccount();

        assertThat(account).isEqualTo(new Account("alice", null, null, 0));
    }

    @Test
    void updateCurrentAccountUpsertsProfileCreatesPendingOperationAndReturnsUpdatedAccount() {
        LocalDate birthdate = LocalDate.of(1990, 1, 2);

        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountRepository.findById("alice")).thenReturn(Optional.of(
            new AccountEntity("alice", "Alice", birthdate, 75)
        ));

        Account account = accountsService.updateCurrentAccount("Alice", birthdate);

        assertThat(account).isEqualTo(new Account("alice", "Alice", birthdate, 75));

        verify(accountRepository).upsertProfile("alice", "Alice", birthdate);

        ArgumentCaptor<ProfileOperationEntity> operationCaptor =
            ArgumentCaptor.forClass(ProfileOperationEntity.class);
        verify(profileOperationRepository).save(operationCaptor.capture());

        ProfileOperationEntity operation = operationCaptor.getValue();
        assertThat(operation.getOperationId()).isNotBlank();
        assertThat(operation.getLogin()).isEqualTo("alice");
        assertThat(operation.getStage()).isEqualTo(ProfileOperationStage.NOTIFICATION_PENDING);
    }

    @Test
    void updateCurrentAccountThrowsWhenUpdatedAccountCannotBeLoaded() {
        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountRepository.findById("alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountsService.updateCurrentAccount("Alice", LocalDate.of(1990, 1, 2)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sendProfileUpdateNotificationRejectsUnexpectedStage() {
        ProfileOperationEntity operation = new ProfileOperationEntity(
            "operation-1",
            "alice",
            ProfileOperationStage.COMPLETED
        );

        assertThatThrownBy(() -> accountsService.sendProfileUpdateNotification(operation))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sendProfileUpdateNotificationCompletesOperationWhenClientSucceeds() {
        ProfileOperationEntity operation = new ProfileOperationEntity(
            "operation-1",
            "alice",
            ProfileOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendProfileUpdateNotification("operation-1", "alice")).thenReturn(true);

        accountsService.sendProfileUpdateNotification(operation);

        assertThat(operation.getStage()).isEqualTo(ProfileOperationStage.COMPLETED);
        verify(profileOperationRepository).save(operation);
    }

    @Test
    void sendProfileUpdateNotificationDoesNothingWhenClientFails() {
        ProfileOperationEntity operation = new ProfileOperationEntity(
            "operation-1",
            "alice",
            ProfileOperationStage.NOTIFICATION_PENDING
        );
        when(notificationsClient.sendProfileUpdateNotification("operation-1", "alice")).thenReturn(false);

        accountsService.sendProfileUpdateNotification(operation);

        assertThat(operation.getStage()).isEqualTo(ProfileOperationStage.NOTIFICATION_PENDING);
        verify(profileOperationRepository, never()).save(operation);
    }

    @Test
    void sendProfileUpdateNotificationSwallowsPersistenceError() {
        ProfileOperationEntity operation = new ProfileOperationEntity(
            "operation-1",
            "alice",
            ProfileOperationStage.NOTIFICATION_PENDING
        );

        when(notificationsClient.sendProfileUpdateNotification("operation-1", "alice")).thenReturn(true);
        doThrow(new RuntimeException("db failure")).when(profileOperationRepository).save(operation);

        assertThatCode(() -> accountsService.sendProfileUpdateNotification(operation))
            .doesNotThrowAnyException();
    }

    @Test
    void getAccountsReturnsMappedListItems() {
        when(accountRepository.findAll()).thenReturn(List.of(
            new AccountEntity("alice", "Alice", LocalDate.of(1990, 1, 2), 100),
            new AccountEntity("bob", "Bob", LocalDate.of(1992, 3, 4), 200)
        ));

        List<AccountListItem> accounts = accountsService.getAccounts();

        assertThat(accounts).containsExactly(
            new AccountListItem("alice", "Alice"),
            new AccountListItem("bob", "Bob")
        );
    }
}
