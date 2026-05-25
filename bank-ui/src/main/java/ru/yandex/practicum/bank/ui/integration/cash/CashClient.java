package ru.yandex.practicum.bank.ui.integration.cash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.integration.cash.model.CashOperationErrorCode;
import ru.yandex.practicum.bank.ui.integration.cash.model.CashOperationErrorResponse;
import ru.yandex.practicum.bank.ui.integration.cash.model.CashOperationRequest;

@Component
public class CashClient {
    private static final Logger log = LoggerFactory.getLogger(CashClient.class);
    private final RestClient cashRestClient;

    public CashClient(RestClient cashRestClient) {
        this.cashRestClient = cashRestClient;
    }

    public BalanceOperationStatus performCashOperation(int amount, CashOperationType type) {
        try {
            int statusCode = cashRestClient.post()
                .body(new CashOperationRequest(type, amount))
                .retrieve()
                .toBodilessEntity()
                .getStatusCode()
                .value();
            return statusCode == 202
                ? BalanceOperationStatus.PROCESSING
                : BalanceOperationStatus.SUCCESS;
        } catch (RestClientResponseException ex) {
            try {
                CashOperationErrorResponse response = ex.getResponseBodyAs(CashOperationErrorResponse.class);
                if (response == null) {
                    log.error("Cash request failed with HTTP status {}, response body is empty", ex.getStatusCode());
                } else if (response.code() == CashOperationErrorCode.INSUFFICIENT_FUNDS) {
                    return BalanceOperationStatus.INSUFFICIENT_FUNDS;
                } else {
                    log.error("Cash request failed with HTTP status {}", ex.getStatusCode());
                }
            } catch (Exception ignored) {
                log.error("Cash request failed with HTTP status {}, failed to parse response", ex.getStatusCode(), ex);
            }
        } catch (Exception ex) {
            log.error("Cash request failed", ex);
        }
        return BalanceOperationStatus.ERROR;
    }
}
