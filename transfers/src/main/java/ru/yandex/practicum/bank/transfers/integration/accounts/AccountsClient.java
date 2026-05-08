package ru.yandex.practicum.bank.transfers.integration.accounts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.integration.accounts.model.AccountsBalanceOperationErrorCode;
import ru.yandex.practicum.bank.transfers.integration.accounts.model.AccountsBalanceOperationErrorResponse;
import ru.yandex.practicum.bank.transfers.integration.accounts.model.AccountsBalanceOperationRequest;

@Component
public class AccountsClient {
    private static final Logger log = LoggerFactory.getLogger(AccountsClient.class);

    private final RestClient accountsRestClient;

    public AccountsClient(RestClient accountsRestClient) {
        this.accountsRestClient = accountsRestClient;
    }

    public BalanceOperationResult performBalanceOperation(
        String operationId,
        String login,
        int amount,
        BalanceOperationType type
    ) {
        try {
            accountsRestClient.post()
                .uri("/{login}/balance", login)
                .header("Operation-Id", operationId)
                .body(new AccountsBalanceOperationRequest(type, amount))
                .retrieve()
                .toBodilessEntity();
            return BalanceOperationResult.SUCCESS;
        } catch (RestClientResponseException ex) {
            try {
                AccountsBalanceOperationErrorResponse response =
                    ex.getResponseBodyAs(AccountsBalanceOperationErrorResponse.class);
                if (response == null) {
                    log.error("Balance request failed with HTTP status {}, response body is empty", ex.getStatusCode());
                } else if (response.code() == AccountsBalanceOperationErrorCode.INSUFFICIENT_FUNDS) {
                    return BalanceOperationResult.INSUFFICIENT_FUNDS;
                } else {
                    log.error("Balance request failed with HTTP status {}", ex.getStatusCode());
                }
            } catch (Exception ignored) {
                log.error(
                    "Balance request failed with HTTP status {}, failed to parse response",
                    ex.getStatusCode(),
                    ex
                );
            }
        } catch (Exception ex) {
            log.error("Balance request failed", ex);
        }
        return BalanceOperationResult.ERROR;
    }
}
