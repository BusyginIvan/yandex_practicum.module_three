package contracts.accounts.update

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Update current account rejects a request without authorization'
    name 'update_me_should_reject_missing_authorization'

    request {
        method PUT()
        url '/accounts/me'
        headers {
            contentType(applicationJson())
            header 'Authorization': absent()
        }
        body(
            name: 'Alice Updated',
            birthdate: '1990-01-02'
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
