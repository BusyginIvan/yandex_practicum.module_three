package ru.yandex.practicum.bank.accounts.integration.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = NotificationsClientContractTestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
    ids = "ru.yandex.practicum:notifications:+:stubs:8180",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = "integration.notifications.base-url=http://localhost:8180/notifications")
class NotificationsClientContractTest {
    @Autowired
    private NotificationsClient notificationsClient;

    @Test
    void sendProfileUpdateNotificationShouldMatchContract() {
        boolean result = notificationsClient.sendProfileUpdateNotification(
            UUID.randomUUID().toString(),
            "alice"
        );
        assertThat(result).isTrue();
    }
}
