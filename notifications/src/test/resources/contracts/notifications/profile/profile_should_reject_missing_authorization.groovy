package contracts.notifications.profile

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Profile notification rejects a request without authorization'
    name 'profile_should_reject_missing_authorization'

    request {
        method POST()
        url '/notifications/profile'
        headers {
            contentType(applicationJson())
            header 'Operation-Id', 'operation-1'
        }
        body(
            login: 'alice'
        )
    }

    response {
        status UNAUTHORIZED()
    }
}
