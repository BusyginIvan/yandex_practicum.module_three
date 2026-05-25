package ru.yandex.practicum.bank.transfers.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.bank.transfers.config.ClientContractTestConfig;

@SpringBootTest(
    classes = ClientContractTestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
    ids = {
        "ru.yandex.practicum:accounts:+:stubs:8281",
        "ru.yandex.practicum:notifications:+:stubs:8280"
    },
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
    "integration.accounts.base-url=http://localhost:8281/accounts",
    "integration.notifications.base-url=http://localhost:8280/notifications"
})
public abstract class AbstractClientContractTest { }
