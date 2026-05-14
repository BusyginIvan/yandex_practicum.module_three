package contracts.accounts.list

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get account list accepts an authorized request'
    name 'list_should_accept_authorized_request'

    request {
        method GET()
        url '/accounts/list'
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
        body([
            [
                login: 'alice',
                name: 'Alice'
            ],
            [
                login: 'bob',
                name: 'Bob'
            ]
        ])
    }
}
