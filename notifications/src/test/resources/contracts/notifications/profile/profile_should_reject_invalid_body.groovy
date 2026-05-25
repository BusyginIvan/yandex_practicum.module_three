package contracts.notifications.profile

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Profile notification rejects invalid body'
    name 'profile_should_reject_invalid_body'

    request {
        method POST()
        url '/notifications/profile'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer profile-token')
            )
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'x' * 256
        )
    }

    response {
        status BAD_REQUEST()
    }
}
