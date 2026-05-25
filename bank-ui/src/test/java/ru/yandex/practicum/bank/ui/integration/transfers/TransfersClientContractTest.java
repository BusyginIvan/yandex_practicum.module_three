package ru.yandex.practicum.bank.ui.integration.transfers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.integration.AbstractClientContractTest;

import static org.assertj.core.api.Assertions.assertThat;

class TransfersClientContractTest extends AbstractClientContractTest {
    @Autowired
    private TransfersClient transfersClient;

    @Test
    void transferReturnsSuccess() {
        BalanceOperationStatus result = transfersClient.transfer(250, "bob");

        assertThat(result).isEqualTo(BalanceOperationStatus.SUCCESS);
    }

    @Test
    void transferReturnsProcessing() {
        BalanceOperationStatus result = transfersClient.transfer(500, "async-bob");

        assertThat(result).isEqualTo(BalanceOperationStatus.PROCESSING);
    }

    @Test
    void transferReturnsInsufficientFunds() {
        BalanceOperationStatus result = transfersClient.transfer(100, "poor-bob");

        assertThat(result).isEqualTo(BalanceOperationStatus.INSUFFICIENT_FUNDS);
    }
}
