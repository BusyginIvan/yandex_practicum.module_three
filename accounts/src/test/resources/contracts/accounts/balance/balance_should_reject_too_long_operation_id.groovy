package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation rejects too long Operation-Id'
    name 'balance_should_reject_too_long_operation_id'

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
                consumer('x' * 256),
                producer('x' * 256)
            )
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
            message: 'Operation-Id must not exceed 255 characters'
        )
    }
}
