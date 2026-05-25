package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer accepts an authorized request'
    name 'transfer_should_accept_authorized_request'

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
            amount: 250
        )
    }

    response {
        status OK()
    }
}
