package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation rejects a request without authorization'
    name 'cash_should_reject_missing_authorization'

    request {
        method POST()
        url '/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization': absent()
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
