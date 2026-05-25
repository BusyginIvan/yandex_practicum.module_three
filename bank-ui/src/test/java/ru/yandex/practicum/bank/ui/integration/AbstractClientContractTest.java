package ru.yandex.practicum.bank.ui.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import ru.yandex.practicum.bank.ui.config.ClientContractTestConfig;

@SpringBootTest(
    classes = ClientContractTestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "integration.accounts.base-url=http://localhost:8181/accounts",
        "integration.cash.base-url=http://localhost:8182/cash",
        "integration.transfers.base-url=http://localhost:8183/transfers"
    }
)
@AutoConfigureStubRunner(
    ids = {
        "ru.yandex.practicum:accounts:+:stubs:8181",
        "ru.yandex.practicum:cash:+:stubs:8182",
        "ru.yandex.practicum:transfers:+:stubs:8183"
    },
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public abstract class AbstractClientContractTest { }
