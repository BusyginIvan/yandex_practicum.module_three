package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation rejects too long login'
    name 'balance_should_reject_too_long_login'

    request {
        method POST()
        url('/accounts/' + ('a' * 256) + '/balance')
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-balance-write-token')
            )
            header 'Operation-Id', 'operation-1'
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
            message: 'Login must not exceed 255 characters'
        )
    }
}
