package ru.yandex.practicum.bank.transfers.integration.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class AccountsClientContractTest extends AbstractClientContractTest {
    @Autowired
    private AccountsClient accountsClient;

    @Test
    void performBalanceOperationReturnsSuccess() {
        BalanceOperationResult result = accountsClient.performBalanceOperation(
            "transfer-balance-success",
            "alice",
            100,
            BalanceOperationType.DEPOSIT
        );

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
    }

    @Test
    void performBalanceOperationReturnsInsufficientFunds() {
        BalanceOperationResult result = accountsClient.performBalanceOperation(
            "operation-insufficient",
            "alice",
            100,
            BalanceOperationType.WITHDRAW
        );

        assertThat(result).isEqualTo(BalanceOperationResult.INSUFFICIENT_FUNDS);
    }
}
