package contracts.notifications.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash notification rejects a token without cash authority'
    name 'cash_should_reject_wrong_authority'

    request {
        method POST()
        url '/notifications/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'alice',
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status FORBIDDEN()
    }
}
