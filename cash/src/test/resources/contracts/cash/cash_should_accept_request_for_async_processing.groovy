package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation accepts a request for asynchronous processing when persistence fails'
    name 'cash_should_accept_request_for_async_processing'

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
            amount: 500
        )
    }

    response {
        status ACCEPTED()
    }
}
