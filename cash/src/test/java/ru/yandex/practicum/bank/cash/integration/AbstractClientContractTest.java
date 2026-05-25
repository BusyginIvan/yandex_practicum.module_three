package ru.yandex.practicum.bank.cash.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.bank.cash.config.ClientContractTestConfig;

@SpringBootTest(
    classes = ClientContractTestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
    ids = {
        "ru.yandex.practicum:accounts:+:stubs:8181",
        "ru.yandex.practicum:notifications:+:stubs:8180"
    },
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
    "integration.accounts.base-url=http://localhost:8181/accounts",
    "integration.notifications.base-url=http://localhost:8180/notifications"
})
public abstract class AbstractClientContractTest { }
