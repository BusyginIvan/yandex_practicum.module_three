package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation rejects conflicting Operation-Id reuse'
    name 'balance_should_reject_operation_id_conflict'

    request {
        method POST()
        url '/accounts/alice/balance'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-balance-write-token')
            )
            header 'Operation-Id', 'operation-conflict'
        }
        body(
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(
            code: 'BAD_REQUEST',
            message: null
        )
    }
}
