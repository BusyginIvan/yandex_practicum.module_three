package contracts.accounts.update

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Update current account rejects an invalid request body'
    name 'update_me_should_reject_invalid_body'

    request {
        method PUT()
        url '/accounts/me'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-write-token')
            )
        }
        body(
            name: 'Alice',
            birthdate: '2999-01-01'
        )
    }

    response {
        status BAD_REQUEST()
    }
}
