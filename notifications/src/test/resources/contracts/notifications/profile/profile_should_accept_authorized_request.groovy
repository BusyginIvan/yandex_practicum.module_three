package contracts.notifications.profile

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Profile notification accepts an authorized request'
    name 'profile_should_accept_authorized_request'

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
                consumer(regex('.+')),
                producer('operation-1')
            )
        }
        body(
            login: 'alice'
        )
    }

    response {
        status OK()
    }
}
