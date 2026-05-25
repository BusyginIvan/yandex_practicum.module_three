package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer returns insufficient funds'
    name 'transfer_should_reject_insufficient_funds'

    request {
        method POST()
        url '/transfers'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer transfers-write-token')
            )
        }
        body(
            recipientLogin: 'poor-bob',
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
