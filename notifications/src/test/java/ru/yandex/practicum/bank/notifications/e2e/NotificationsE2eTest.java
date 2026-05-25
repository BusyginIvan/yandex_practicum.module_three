package ru.yandex.practicum.bank.notifications.e2e;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.bank.notifications.config.PostgresTestConfig;
import ru.yandex.practicum.bank.notifications.config.SecurityTestConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@Import({PostgresTestConfig.class, SecurityTestConfig.class})
public class NotificationsE2eTest {
    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        jdbc.getJdbcTemplate().execute("TRUNCATE TABLE processed_notifications");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestSpecs")
    void processedNotificationPersistsOnlyOnceForAuthorizedUser(
        RequestSpec requestSpec,
        CapturedOutput output
    ) throws Exception {
        String firstOperationId = UUID.randomUUID().toString();
        String secondOperationId = UUID.randomUUID().toString();

        performAuthorizedNotificationRequest(requestSpec, firstOperationId)
            .andExpect(status().isOk());

        assertProcessedNotificationsRecorded(firstOperationId);
        assertNotificationLoggedTimes(output, requestSpec.expectedLogMessage(), 1);

        performAuthorizedNotificationRequest(requestSpec, firstOperationId)
            .andExpect(status().isOk());

        assertProcessedNotificationsRecorded(firstOperationId);
        assertNotificationLoggedTimes(output, requestSpec.expectedLogMessage(), 1);

        performAuthorizedNotificationRequest(requestSpec, secondOperationId)
            .andExpect(status().isOk());

        assertProcessedNotificationsRecorded(firstOperationId, secondOperationId);
        assertNotificationLoggedTimes(output, requestSpec.expectedLogMessage(), 2);
    }

    private ResultActions performAuthorizedNotificationRequest(
        RequestSpec requestSpec,
        String operationId
    ) throws Exception {
        return mockMvc.perform(post(requestSpec.path())
            .with(jwt().authorities(new SimpleGrantedAuthority(requestSpec.authority())))
            .header("Operation-Id", operationId)
            .contentType("application/json")
            .content(requestSpec.requestBody()));
    }

    private void assertProcessedNotificationsRecorded(String... operationIds) {
        List<String> recordedOperationIds = jdbc.queryForList(
            "select operation_id from processed_notifications",
            Map.of(),
            String.class
        );
        assertThat(recordedOperationIds).containsExactlyInAnyOrder(operationIds);
    }

    private void assertNotificationLoggedTimes(CapturedOutput output, String logMessage, int expectedCount) {
        assertThat(output.getOut().split(Pattern.quote(logMessage), -1).length - 1)
            .isEqualTo(expectedCount);
    }

    private static Stream<RequestSpec> requestSpecs() {
        return Stream.of(
            new RequestSpec(
                "cash",
                "/notifications/cash",
                "notifications:cash",
                "Notification for user alice: deposited 100 rubles.",
                """
                    {
                      "login": "alice",
                      "type": "DEPOSIT",
                      "amount": 100
                    }
                    """
            ),
            new RequestSpec(
                "profile",
                "/notifications/profile",
                "notifications:profile",
                "Notification for user alice: profile details were updated.",
                """
                    {
                      "login": "alice"
                    }
                    """
            ),
            new RequestSpec(
                "transfer",
                "/notifications/transfer",
                "notifications:transfer",
                "Notification for user alice: transferred 250 rubles to user bob.",
                """
                    {
                      "senderLogin": "alice",
                      "recipientLogin": "bob",
                      "amount": 250
                    }
                    """
            )
        );
    }

    private record RequestSpec(
        String name,
        String path,
        String authority,
        String expectedLogMessage,
        String requestBody
    ) {
        @Override
        public @NotNull String toString() {
            return name;
        }
    }
}
