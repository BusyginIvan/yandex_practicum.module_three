package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation accepts an authorized request'
    name 'cash_should_accept_authorized_request'

    request {
        method POST()
        url '/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer cash-write-token')
            )
        }
        body(
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status OK()
    }
}
