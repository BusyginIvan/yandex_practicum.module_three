package contracts.notifications.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash notification rejects too long Operation-Id'
    name 'cash_should_reject_too_long_operation_id'

    request {
        method POST()
        url '/notifications/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer cash-token')
            )
            header 'Operation-Id', value(
                consumer('x' * 256),
                producer('x' * 256)
            )
        }
        body(
            login: 'alice',
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
    }
}
