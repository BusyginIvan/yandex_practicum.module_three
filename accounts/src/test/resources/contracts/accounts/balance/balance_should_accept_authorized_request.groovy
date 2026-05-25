package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation accepts an authorized request'
    name 'balance_should_accept_authorized_request'

    request {
        method POST()
        url '/accounts/alice/balance'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-balance-write-token')
            )
            header 'Operation-Id', value(
                consumer(regex('.+')),
                producer('operation-1')
            )
        }
        body(
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status OK()
    }
}
