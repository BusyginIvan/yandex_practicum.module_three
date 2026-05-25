package contracts.accounts.me

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get current account rejects a request without authorization'
    name 'get_me_should_reject_missing_authorization'

    request {
        method GET()
        url '/accounts/me'
        headers {
            header 'Authorization': absent()
        }
    }

    response {
        status UNAUTHORIZED()
    }
}
