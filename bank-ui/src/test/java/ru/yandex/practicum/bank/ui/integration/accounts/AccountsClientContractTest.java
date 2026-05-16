package ru.yandex.practicum.bank.ui.integration.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.integration.AbstractClientContractTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountsClientContractTest extends AbstractClientContractTest {
    @Autowired
    private AccountsClient accountsClient;

    @Test
    void getMeMatchesContract() {
        AccountDetails account = accountsClient.getMe();

        assertThat(account).isEqualTo(new AccountDetails(
            "alice",
            "Alice",
            LocalDate.of(1990, 1, 2),
            100
        ));
    }

    @Test
    void updateMeMatchesContract() {
        AccountDetails account = accountsClient.updateMe("Alice Updated", LocalDate.of(1990, 1, 2));

        assertThat(account).isEqualTo(new AccountDetails(
            "alice",
            "Alice Updated",
            LocalDate.of(1990, 1, 2),
            100
        ));
    }

    @Test
    void getAccountsMatchesContract() {
        List<AccountListItem> accounts = accountsClient.getAccounts();

        assertThat(accounts).containsExactly(
            new AccountListItem("alice", "Alice"),
            new AccountListItem("bob", "Bob")
        );
    }
}
