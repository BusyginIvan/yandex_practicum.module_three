package contracts.accounts.me

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get current account accepts an authorized request'
    name 'get_me_should_accept_authorized_request'

    request {
        method GET()
        url '/accounts/me'
        headers {
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer accounts-read-token')
            )
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            login: 'alice',
            name: 'Alice',
            birthdate: '1990-01-02',
            balance: 100
        )
    }
}
