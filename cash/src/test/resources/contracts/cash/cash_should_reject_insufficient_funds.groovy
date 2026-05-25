package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation returns insufficient funds'
    name 'cash_should_reject_insufficient_funds'

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
            type: 'WITHDRAW',
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(
            code: 'INSUFFICIENT_FUNDS',
            message: null
        )
    }
}
