package contracts.notifications.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Transfer notification accepts an authorized request'
    name 'transfer_should_accept_authorized_request'

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
                consumer(regex('.+')),
                producer('operation-1')
            )
        }
        body(
            senderLogin: 'alice',
            recipientLogin: 'bob',
            amount: 250
        )
    }

    response {
        status OK()
    }
}
