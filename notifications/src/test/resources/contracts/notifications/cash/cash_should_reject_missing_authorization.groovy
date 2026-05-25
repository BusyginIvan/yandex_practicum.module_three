package contracts.notifications.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash notification rejects a request without authorization'
    name 'cash_should_reject_missing_authorization'

    request {
        method POST()
        url '/notifications/cash'
        headers {
            contentType(applicationJson())
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'alice',
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
