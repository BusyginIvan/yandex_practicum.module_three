package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer rejects a request with a wrong authority'
    name 'transfer_should_reject_wrong_authority'

    request {
        method POST()
        url '/transfers'
        headers {
            contentType(applicationJson())
            header 'Authorization', 'Bearer wrong-token'
        }
        body(
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status FORBIDDEN()
    }
}
