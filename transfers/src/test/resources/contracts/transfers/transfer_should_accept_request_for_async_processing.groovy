package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer accepts a request for async processing'
    name 'transfer_should_accept_request_for_async_processing'

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
            recipientLogin: 'async-bob',
            amount: 500
        )
    }

    response {
        status ACCEPTED()
    }
}
