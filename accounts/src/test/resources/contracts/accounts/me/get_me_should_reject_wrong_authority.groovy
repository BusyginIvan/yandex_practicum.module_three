package contracts.accounts.me

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get current account rejects a token without read authority'
    name 'get_me_should_reject_wrong_authority'

    request {
        method GET()
        url '/accounts/me'
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
