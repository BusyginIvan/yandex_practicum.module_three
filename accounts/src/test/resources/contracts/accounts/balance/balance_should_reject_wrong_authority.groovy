package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation rejects a token without balance authority'
    name 'balance_should_reject_wrong_authority'

    request {
        method POST()
        url '/accounts/alice/balance'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status FORBIDDEN()
    }
}
