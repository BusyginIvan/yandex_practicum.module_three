package contracts.notifications.profile

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Profile notification rejects a token without profile authority'
    name 'profile_should_reject_wrong_authority'

    request {
        method POST()
        url '/notifications/profile'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'alice'
        )
    }

    response {
        status FORBIDDEN()
    }
}
