package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer rejects an invalid request body'
    name 'transfer_should_reject_invalid_body'

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
            recipientLogin: 'bob',
            amount: 0
        )
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(
            code: 'BAD_REQUEST',
            message: 'Amount must be greater than zero'
        )
    }
}
