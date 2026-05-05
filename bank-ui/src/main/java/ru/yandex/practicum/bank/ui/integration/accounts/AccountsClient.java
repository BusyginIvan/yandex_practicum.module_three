package ru.yandex.practicum.bank.ui.integration.accounts;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.integration.accounts.model.UpdateAccountRequest;

import java.time.LocalDate;
import java.util.List;

@Component
public class AccountsClient {
    private static final ParameterizedTypeReference<List<AccountListItem>> ACCOUNTS_LIST_TYPE =
        new ParameterizedTypeReference<>() { };

    private final RestClient accountsRestClient;

    public AccountsClient(RestClient accountsRestClient) {
        this.accountsRestClient = accountsRestClient;
    }

    public AccountDetails getMe() {
        return accountsRestClient.get()
            .uri("/me")
            .retrieve()
            .body(AccountDetails.class);
    }

    public AccountDetails updateMe(String name, LocalDate birthdate) {
        return accountsRestClient.put()
            .uri("/me")
            .body(new UpdateAccountRequest(name, birthdate))
            .retrieve()
            .body(AccountDetails.class);
    }

    public List<AccountListItem> getAccounts() {
        List<AccountListItem> accounts = accountsRestClient.get()
            .uri("/list")
            .retrieve()
            .body(ACCOUNTS_LIST_TYPE);
        return accounts == null ? List.of() : accounts;
    }
}
