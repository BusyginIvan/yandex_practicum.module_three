package ru.yandex.practicum.bank.cash.e2e;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.cash.config.CashE2eTestConfig;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;
import ru.yandex.practicum.bank.cash.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.cash.integration.notifications.NotificationsClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CashE2eTestConfig.class)
class CashE2eTest {

    private static final String LOGIN = "smoke-user";

    @Autowired private MockMvc mockMvc;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    @MockitoBean private AccountsClient accountsClient;
    @MockitoBean private NotificationsClient notificationsClient;

    @BeforeEach
    void beforeEach() {
        jdbc.getJdbcTemplate().execute("TRUNCATE TABLE cash_operations");
        reset(
            accountsClient,
            notificationsClient
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestSpecs")
    void cashOperationUsesCurrentAccountAndPersistsExpectedState(RequestSpec requestSpec) throws Exception {
        when(accountsClient.performBalanceOperation(
            any(String.class),
            eq(LOGIN),
            eq(requestSpec.amount()),
            eq(requestSpec.type())
        )).thenReturn(requestSpec.result());

        mockMvc.perform(post("/cash")
                .with(jwt().jwt(token -> token.claim("preferred_username", LOGIN))
                    .authorities(new SimpleGrantedAuthority("cash:write")))
                .contentType("application/json")
                .content(requestSpec.body()))
            .andExpect(status().is(requestSpec.expectedStatus()));

        Map<String, Object> persistedOperation = loadPersistedOperation();
        String operationId = persistedOperation.get("operation_id").toString();

        verify(accountsClient).performBalanceOperation(
            operationId,
            LOGIN,
            requestSpec.amount(),
            requestSpec.type()
        );
        verify(notificationsClient, never()).sendCashNotification(any(), any(), any(Integer.class), any());

        assertThat(persistedOperation)
            .containsEntry("login", LOGIN)
            .containsEntry("type", requestSpec.type().name())
            .containsEntry("amount", requestSpec.amount())
            .containsEntry("stage", requestSpec.expectedStage().name());
    }

    private static Stream<RequestSpec> requestSpecs() {
        return Stream.of(
            new RequestSpec()
                .name("success deposit")
                .body("""
                    {
                      "type": "DEPOSIT",
                      "amount": 100
                    }
                    """)
                .amount(100)
                .type(BalanceOperationType.DEPOSIT)
                .result(BalanceOperationResult.SUCCESS)
                .expectedStatus(200)
                .expectedStage(CashOperationStage.NOTIFICATION_PENDING),
            new RequestSpec()
                .name("insufficient funds withdraw")
                .body("""
                    {
                      "type": "WITHDRAW",
                      "amount": 100
                    }
                    """)
                .amount(100)
                .type(BalanceOperationType.WITHDRAW)
                .result(BalanceOperationResult.INSUFFICIENT_FUNDS)
                .expectedStatus(400)
                .expectedStage(CashOperationStage.REJECTED_INSUFFICIENT_FUNDS)
        );
    }

    private Map<String, Object> loadPersistedOperation() {
        List<Map<String, Object>> operations = jdbc.queryForList(
            """
                select operation_id, login, type, amount, stage
                from cash_operations
                """,
            Map.of()
        );

        assertThat(operations).hasSize(1);
        return operations.getFirst();
    }

    @Getter @Setter
    @Accessors(chain = true, fluent = true)
    private static final class RequestSpec {
        private String name;
        private String body;
        private int amount;
        private BalanceOperationType type;
        private BalanceOperationResult result;
        private int expectedStatus;
        private CashOperationStage expectedStage;

        @Override
        public @NotNull String toString() {
            return name;
        }
    }
}
