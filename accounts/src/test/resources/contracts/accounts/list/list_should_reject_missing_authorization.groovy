package contracts.accounts.list

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Get account list rejects a request without authorization'
    name 'list_should_reject_missing_authorization'

    request {
        method GET()
        url '/accounts/list'
        headers {
            header 'Authorization': absent()
        }
    }

    response {
        status UNAUTHORIZED()
    }
}
