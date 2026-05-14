package contracts.accounts.balance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Balance operation rejects a request without authorization'
    name 'balance_should_reject_missing_authorization'

    request {
        method POST()
        url '/accounts/alice/balance'
        headers {
            contentType(applicationJson())
            header 'Operation-Id', 'operation-1'
        }
        body(
            type: 'DEPOSIT',
            amount: 100
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
