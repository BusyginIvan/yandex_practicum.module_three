package ru.yandex.practicum.bank.accounts.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;
import ru.yandex.practicum.bank.accounts.exception.OperationIdConflictException;
import ru.yandex.practicum.bank.accounts.persistence.entity.BalanceOperationEntity;
import ru.yandex.practicum.bank.accounts.support.AbstractBusinessLayerTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BalanceServiceTest extends AbstractBusinessLayerTest {
    @Autowired
    private BalanceService balanceService;

    @Test
    void performDepositCreatesOperationAndFinalizesSuccess() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(1);
        when(accountRepository.deposit("alice", 100))
            .thenReturn(1);
        when(balanceOperationRepository.updateStatus("operation-1", BalanceOperationStatus.SUCCESS))
            .thenReturn(1);

        BalanceOperationResult result = balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        );

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
        verify(accountRepository).createIfMissing("alice");
        verify(accountRepository).deposit("alice", 100);
        verify(balanceOperationRepository).updateStatus("operation-1", BalanceOperationStatus.SUCCESS);
    }

    @Test
    void performWithdrawReturnsInsufficientFundsAndFinalizesOperation() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.WITHDRAW,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(1);
        when(accountRepository.withdrawIfEnough("alice", 100))
            .thenReturn(0);
        when(balanceOperationRepository.updateStatus(
            "operation-1",
            BalanceOperationStatus.INSUFFICIENT_FUNDS
        )).thenReturn(1);

        BalanceOperationResult result = balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.WITHDRAW,
            100
        );

        assertThat(result).isEqualTo(BalanceOperationResult.INSUFFICIENT_FUNDS);
        verify(accountRepository).withdrawIfEnough("alice", 100);
        verify(balanceOperationRepository)
            .updateStatus("operation-1", BalanceOperationStatus.INSUFFICIENT_FUNDS);
    }

    @Test
    void performBalanceOperationReturnsExistingResultForDuplicateSuccess() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(0);
        when(balanceOperationRepository.findById("operation-1")).thenReturn(Optional.of(
            new BalanceOperationEntity(
                "operation-1",
                "alice",
                BalanceOperationType.DEPOSIT,
                100,
                BalanceOperationStatus.SUCCESS
            )
        ));

        BalanceOperationResult result = balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        );

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
        verify(accountRepository, never()).deposit("alice", 100);
        verify(balanceOperationRepository, never()).updateStatus("operation-1", BalanceOperationStatus.SUCCESS);
    }

    @Test
    void performBalanceOperationRejectsDuplicateOperationIdForAnotherRequest() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(0);
        when(balanceOperationRepository.findById("operation-1")).thenReturn(Optional.of(
            new BalanceOperationEntity(
                "operation-1",
                "alice",
                BalanceOperationType.DEPOSIT,
                200,
                BalanceOperationStatus.SUCCESS
            )
        ));

        assertThatThrownBy(() -> balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        )).isInstanceOf(OperationIdConflictException.class);
    }

    @Test
    void performBalanceOperationRejectsDuplicateOperationThatIsStillProcessing() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(0);
        when(balanceOperationRepository.findById("operation-1")).thenReturn(Optional.of(
            new BalanceOperationEntity(
                "operation-1",
                "alice",
                BalanceOperationType.DEPOSIT,
                100,
                BalanceOperationStatus.PROCESSING
            )
        ));

        assertThatThrownBy(() -> balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void performDepositThrowsWhenAccountCannotBeUpdated() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(1);
        when(accountRepository.deposit("alice", 100))
            .thenReturn(0);

        assertThatThrownBy(() -> balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        )).isInstanceOf(IllegalStateException.class);

        verify(balanceOperationRepository, never()).updateStatus("operation-1", BalanceOperationStatus.SUCCESS);
    }

    @Test
    void performBalanceOperationThrowsWhenOperationCannotBeFinalized() {
        when(balanceOperationRepository.createIfMissing(
            "operation-1",
            "alice",
            BalanceOperationType.DEPOSIT,
            100,
            BalanceOperationStatus.PROCESSING
        )).thenReturn(1);
        when(accountRepository.deposit("alice", 100))
            .thenReturn(1);
        when(balanceOperationRepository.updateStatus("operation-1", BalanceOperationStatus.SUCCESS))
            .thenReturn(0);

        assertThatThrownBy(() -> balanceService.performBalanceOperation(
            "alice",
            "operation-1",
            BalanceOperationType.DEPOSIT,
            100
        )).isInstanceOf(IllegalStateException.class);
    }
}
