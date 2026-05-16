package ru.yandex.practicum.bank.ui.integration.cash;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class CashClientContractTest extends AbstractClientContractTest {
    @Autowired
    private CashClient cashClient;

    @Test
    void performCashOperationReturnsSuccess() {
        BalanceOperationStatus result = cashClient.performCashOperation(100, CashOperationType.DEPOSIT);

        assertThat(result).isEqualTo(BalanceOperationStatus.SUCCESS);
    }

    @Test
    void performCashOperationReturnsProcessing() {
        BalanceOperationStatus result = cashClient.performCashOperation(500, CashOperationType.DEPOSIT);

        assertThat(result).isEqualTo(BalanceOperationStatus.PROCESSING);
    }

    @Test
    void performCashOperationReturnsInsufficientFunds() {
        BalanceOperationStatus result = cashClient.performCashOperation(100, CashOperationType.WITHDRAW);

        assertThat(result).isEqualTo(BalanceOperationStatus.INSUFFICIENT_FUNDS);
    }
}
