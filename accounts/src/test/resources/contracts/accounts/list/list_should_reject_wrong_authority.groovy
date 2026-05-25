package contracts.accounts.list

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get account list rejects a token without read authority'
    name 'list_should_reject_wrong_authority'

    request {
        method GET()
        url '/accounts/list'
        headers {
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
        }
    }

    response {
        status FORBIDDEN()
    }
}
