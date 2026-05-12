package contracts.notifications.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer notification rejects a token without transfer authority'
    name 'transfer_should_reject_wrong_authority'

    request {
        method POST()
        url '/notifications/transfer'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            senderLogin: 'alice',
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status FORBIDDEN()
    }
}
