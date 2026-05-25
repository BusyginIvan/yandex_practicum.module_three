package contracts.notifications.profile

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Profile notification rejects too long Operation-Id'
    name 'profile_should_reject_too_long_operation_id'

    request {
        method POST()
        url '/notifications/profile'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer(regex('Bearer\\s+.+')),
                producer('Bearer profile-token')
            )
            header 'Operation-Id', value(
                consumer('x' * 256),
                producer('x' * 256)
            )
        }
        body(
            login: 'alice'
        )
    }

    response {
        status BAD_REQUEST()
    }
}
