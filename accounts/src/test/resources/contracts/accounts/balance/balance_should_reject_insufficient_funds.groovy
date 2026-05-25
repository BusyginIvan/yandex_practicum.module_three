package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation returns insufficient funds'
    name 'balance_should_reject_insufficient_funds'

    request {
        method POST()
        url '/accounts/alice/balance'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-balance-write-token')
            )
            header 'Operation-Id', 'operation-insufficient'
        }
        body(
            type: 'WITHDRAW',
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(
            code: 'INSUFFICIENT_FUNDS',
            message: null
        )
    }
}
