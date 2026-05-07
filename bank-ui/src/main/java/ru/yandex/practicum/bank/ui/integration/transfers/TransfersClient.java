package ru.yandex.practicum.bank.ui.integration.transfers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.integration.transfers.model.TransferOperationErrorCode;
import ru.yandex.practicum.bank.ui.integration.transfers.model.TransferOperationErrorResponse;
import ru.yandex.practicum.bank.ui.integration.transfers.model.TransferRequest;

@Component
public class TransfersClient {
    private static final Logger log = LoggerFactory.getLogger(TransfersClient.class);
    private final RestClient transfersRestClient;

    public TransfersClient(RestClient transfersRestClient) {
        this.transfersRestClient = transfersRestClient;
    }

    public BalanceOperationStatus transfer(int amount, String recipientLogin) {
        try {
            int statusCode = transfersRestClient.post()
                .body(new TransferRequest(recipientLogin, amount))
                .retrieve()
                .toBodilessEntity()
                .getStatusCode()
                .value();
            return statusCode == 202
                ? BalanceOperationStatus.PROCESSING
                : BalanceOperationStatus.SUCCESS;
        } catch (RestClientResponseException ex) {
            try {
                TransferOperationErrorResponse response = ex.getResponseBodyAs(TransferOperationErrorResponse.class);
                if (response == null) {
                    log.error("Transfer request failed with HTTP status {}, response body is empty", ex.getStatusCode());
                } else if (response.code() == TransferOperationErrorCode.INSUFFICIENT_FUNDS) {
                    return BalanceOperationStatus.INSUFFICIENT_FUNDS;
                } else {
                    log.error("Transfer request failed with HTTP status {}", ex.getStatusCode());
                }
            } catch (Exception ignored) {
                log.error("Transfer request failed with HTTP status {}, failed to parse response", ex.getStatusCode(), ex);
            }
        } catch (Exception ex) {
            log.error("Transfer request failed", ex);
        }
        return BalanceOperationStatus.ERROR;
    }
}
