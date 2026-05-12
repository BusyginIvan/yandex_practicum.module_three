package contracts.notifications.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer notification rejects too long Operation-Id'
    name 'transfer_should_reject_too_long_operation_id'

    request {
        method POST()
        url '/notifications/transfer'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer transfer-token')
            )
            header 'Operation-Id', value(
                consumer('x' * 256),
                producer('x' * 256)
            )
        }
        body(
            senderLogin: 'alice',
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status BAD_REQUEST()
    }
}
