package contracts.transfers

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer rejects a request without authorization'
    name 'transfer_should_reject_missing_authorization'

    request {
        method POST()
        url '/transfers'
        headers {
            contentType(applicationJson())
            header 'Authorization': absent()
        }
        body(
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
