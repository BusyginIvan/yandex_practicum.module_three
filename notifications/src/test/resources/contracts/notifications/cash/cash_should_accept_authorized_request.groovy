package contracts.notifications.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash notification accepts an authorized request'
    name 'cash_should_accept_authorized_request'

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
                consumer(regex('.+')),
                producer('operation-1')
            )
        }
        body(
            login: 'alice',
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status OK()
    }
}
