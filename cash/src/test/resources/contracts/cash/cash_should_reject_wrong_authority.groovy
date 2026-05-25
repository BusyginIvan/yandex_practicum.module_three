package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Cash operation rejects a token without the required authority'
    name 'cash_should_reject_wrong_authority'

    request {
        method POST()
        url '/cash'
        headers {
            contentType(applicationJson())
            header 'Authorization', 'Bearer wrong-token'
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
