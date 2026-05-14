package contracts.accounts.update

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Update current account accepts an authorized request'
    name 'update_me_should_accept_authorized_request'

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
            name: 'Alice Updated',
            birthdate: '1990-01-02'
        )
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            login: 'alice',
            name: 'Alice Updated',
            birthdate: '1990-01-02',
            balance: 100
        )
    }
}
