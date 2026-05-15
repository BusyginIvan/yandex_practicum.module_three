package ru.yandex.practicum.bank.cash.integration.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class AccountsClientContractTest extends AbstractClientContractTest {

    @Autowired
    private AccountsClient accountsClient;

    @Test
    void performBalanceOperationShouldMatchSuccessContract() {
        BalanceOperationResult result = accountsClient.performBalanceOperation(
            "cash-operation-success",
            "alice",
            100,
            BalanceOperationType.DEPOSIT
        );

        assertThat(result).isEqualTo(BalanceOperationResult.SUCCESS);
    }

    @Test
    void performBalanceOperationShouldMatchInsufficientFundsContract() {
        BalanceOperationResult result = accountsClient.performBalanceOperation(
            "operation-insufficient",
            "alice",
            100,
            BalanceOperationType.WITHDRAW
        );

        assertThat(result).isEqualTo(BalanceOperationResult.INSUFFICIENT_FUNDS);
    }
}
