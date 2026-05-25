package ru.yandex.practicum.bank.transfers.e2e;

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
import ru.yandex.practicum.bank.transfers.config.TransfersE2eTestConfig;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.transfers.domain.BalanceOperationType;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationStage;
import ru.yandex.practicum.bank.transfers.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.transfers.integration.notifications.NotificationsClient;

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
@SpringBootTest(classes = TransfersE2eTestConfig.class)
class TransfersE2eTest {

    private static final String LOGIN = "smoke-user";

    @Autowired private MockMvc mockMvc;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    @MockitoBean private AccountsClient accountsClient;
    @MockitoBean private NotificationsClient notificationsClient;

    @BeforeEach
    void beforeEach() {
        jdbc.getJdbcTemplate().execute("TRUNCATE TABLE transfer_operations");
        reset(
            accountsClient,
            notificationsClient
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestSpecs")
    void transferUsesCurrentAccountAndPersistsExpectedState(RequestSpec requestSpec) throws Exception {
        when(accountsClient.performBalanceOperation(
            any(String.class),
            eq(LOGIN),
            eq(requestSpec.amount()),
            eq(BalanceOperationType.WITHDRAW)
        )).thenReturn(requestSpec.withdrawResult());
        when(accountsClient.performBalanceOperation(
            any(String.class),
            eq(requestSpec.recipientLogin()),
            eq(requestSpec.amount()),
            eq(BalanceOperationType.DEPOSIT)
        )).thenReturn(requestSpec.depositResult());

        mockMvc.perform(post("/transfers")
                .with(jwt().jwt(token -> token.claim("preferred_username", LOGIN))
                    .authorities(new SimpleGrantedAuthority("transfers:write")))
                .contentType("application/json")
                .content(requestSpec.body()))
            .andExpect(status().is(requestSpec.expectedStatus()));

        Map<String, Object> persistedOperation = loadPersistedOperation();
        String withdrawOperationId = persistedOperation.get("withdraw_operation_id").toString();
        String depositOperationId = persistedOperation.get("deposit_operation_id").toString();

        verify(accountsClient).performBalanceOperation(
            withdrawOperationId,
            LOGIN,
            requestSpec.amount(),
            BalanceOperationType.WITHDRAW
        );

        if (requestSpec.expectedStage() == TransferOperationStage.NOTIFICATION_PENDING) {
            verify(accountsClient).performBalanceOperation(
                depositOperationId,
                requestSpec.recipientLogin(),
                requestSpec.amount(),
                BalanceOperationType.DEPOSIT
            );
        }

        verify(notificationsClient, never()).sendTransferNotification(any(), any(), any(), any(Integer.class));

        assertThat(persistedOperation)
            .containsEntry("sender_login", LOGIN)
            .containsEntry("recipient_login", requestSpec.recipientLogin())
            .containsEntry("amount", requestSpec.amount())
            .containsEntry("stage", requestSpec.expectedStage().name());
    }

    private static Stream<RequestSpec> requestSpecs() {
        return Stream.of(
            new RequestSpec()
                .name("success transfer")
                .body("""
                    {
                      "recipientLogin": "bob",
                      "amount": 250
                    }
                    """)
                .recipientLogin("bob")
                .amount(250)
                .withdrawResult(BalanceOperationResult.SUCCESS)
                .depositResult(BalanceOperationResult.SUCCESS)
                .expectedStatus(200)
                .expectedStage(TransferOperationStage.NOTIFICATION_PENDING),
            new RequestSpec()
                .name("insufficient funds transfer")
                .body("""
                    {
                      "recipientLogin": "bob",
                      "amount": 250
                    }
                    """)
                .recipientLogin("bob")
                .amount(250)
                .withdrawResult(BalanceOperationResult.INSUFFICIENT_FUNDS)
                .expectedStatus(400)
                .expectedStage(TransferOperationStage.REJECTED_INSUFFICIENT_FUNDS)
        );
    }

    private Map<String, Object> loadPersistedOperation() {
        List<Map<String, Object>> operations = jdbc.queryForList(
            """
                select operation_id, sender_login, recipient_login, amount, withdraw_operation_id, deposit_operation_id, stage
                from transfer_operations
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
        private String recipientLogin;
        private int amount;
        private BalanceOperationResult withdrawResult;
        private BalanceOperationResult depositResult;
        private int expectedStatus;
        private TransferOperationStage expectedStage;

        @Override
        public @NotNull String toString() {
            return name;
        }
    }
}
