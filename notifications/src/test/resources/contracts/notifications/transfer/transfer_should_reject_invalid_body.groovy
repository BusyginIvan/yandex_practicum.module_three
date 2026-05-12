package contracts.notifications.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer notification rejects invalid body'
    name 'transfer_should_reject_invalid_body'

    request {
        method POST()
        url '/notifications/transfer'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer transfer-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            senderLogin: 'alice',
            recipientLogin: 'bob',
            amount: 0
        )
    }

    response {
        status BAD_REQUEST()
    }
}
