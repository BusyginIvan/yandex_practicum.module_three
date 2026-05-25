package contracts.notifications.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer notification rejects a request without authorization'
    name 'transfer_should_reject_missing_authorization'

    request {
        method POST()
        url '/notifications/transfer'
        headers {
            contentType(applicationJson())
            header 'Operation-Id', 'operation-1'
        }
        body(
            senderLogin: 'alice',
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
