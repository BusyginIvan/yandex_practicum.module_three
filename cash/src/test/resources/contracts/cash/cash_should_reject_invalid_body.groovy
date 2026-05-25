package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation rejects an invalid request body'
    name 'cash_should_reject_invalid_body'

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
            type: null,
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
    }
}
