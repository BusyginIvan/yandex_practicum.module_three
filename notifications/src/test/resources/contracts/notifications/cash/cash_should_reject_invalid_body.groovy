package contracts.notifications.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash notification rejects invalid body'
    name 'cash_should_reject_invalid_body'

    request {
        method POST()
        url '/notifications/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer cash-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'alice',
            type: null,
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
    }
}
