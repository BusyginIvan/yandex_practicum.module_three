package contracts.accounts.update

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'Update current account rejects a token without write authority'
    name 'update_me_should_reject_wrong_authority'

    request {
        method PUT()
        url '/accounts/me'
        headers {
            contentType(applicationJson())
            header 'Authorization', value(
                consumer('Bearer wrong-token'),
                producer('Bearer wrong-token')
            )
        }
        body(
            name: 'Alice Updated',
            birthdate: '1990-01-02'
        )
    }

    response {
        status FORBIDDEN()
    }
}
