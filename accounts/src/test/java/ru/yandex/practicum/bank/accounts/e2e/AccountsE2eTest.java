package ru.yandex.practicum.bank.accounts.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.accounts.config.AccountsE2eTestConfig;
import ru.yandex.practicum.bank.accounts.integration.notifications.NotificationsClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = AccountsE2eTestConfig.class)
class AccountsE2eTest {

    private static final String LOGIN = "alice";

    @Autowired private MockMvc mockMvc;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    @MockitoBean private NotificationsClient notificationsClient;

    @BeforeEach
    void beforeEach() {
        jdbc.getJdbcTemplate().execute("""
            TRUNCATE TABLE
                profile_operations,
                balance_operations,
                accounts
            """);
        reset(notificationsClient);
    }

    @Test
    void getCurrentAccountUsesJwtLoginAndReturnsPersistedAccount() throws Exception {
        insertAccount(LOGIN, "Alice", LocalDate.of(1990, 1, 2), 700);

        mockMvc.perform(get("/accounts/me")
                .with(jwt().jwt(token -> token.claim("preferred_username", LOGIN))
                    .authorities(new SimpleGrantedAuthority("accounts:read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(LOGIN))
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.birthdate").value("1990-01-02"))
            .andExpect(jsonPath("$.balance").value(700));
    }

    @Test
    void updateCurrentAccountUsesJwtLoginAndPersistsProfileOperation() throws Exception {
        mockMvc.perform(put("/accounts/me")
                .with(jwt().jwt(token -> token.claim("preferred_username", LOGIN))
                    .authorities(new SimpleGrantedAuthority("accounts:write")))
                .contentType("application/json")
                .content("""
                    {
                      "name": "Alice",
                      "birthdate": "1990-01-02"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(LOGIN))
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.birthdate").value("1990-01-02"))
            .andExpect(jsonPath("$.balance").value(0));

        assertThat(loadAccounts()).hasSize(1);
        assertThat(loadAccounts().getFirst())
            .containsEntry("login", LOGIN)
            .containsEntry("name", "Alice")
            .containsEntry("birthdate", java.sql.Date.valueOf(LocalDate.of(1990, 1, 2)))
            .containsEntry("balance", 0);

        List<Map<String, Object>> operations = jdbc.queryForList(
            """
                select login, stage
                from profile_operations
                """,
            Map.of()
        );

        assertThat(operations).hasSize(1);
        assertThat(operations.getFirst())
            .containsEntry("login", LOGIN)
            .containsEntry("stage", "NOTIFICATION_PENDING");
    }

    @Test
    void balanceOperationPersistsOperationAndUpdatesBalance() throws Exception {
        insertAccount("bob", "Bob", LocalDate.of(1985, 6, 15), 500);

        mockMvc.perform(post("/accounts/bob/balance")
                .with(jwt().jwt(token -> token.claim("preferred_username", LOGIN))
                    .authorities(new SimpleGrantedAuthority("accounts:balance:write")))
                .header("Operation-Id", "smoke-balance-operation")
                .contentType("application/json")
                .content("""
                    {
                      "type": "WITHDRAW",
                      "amount": 200
                    }
                    """))
            .andExpect(status().isOk());

        assertThat(loadAccounts()).hasSize(1);
        assertThat(loadAccounts().getFirst())
            .containsEntry("login", "bob")
            .containsEntry("name", "Bob")
            .containsEntry("birthdate", java.sql.Date.valueOf(LocalDate.of(1985, 6, 15)))
            .containsEntry("balance", 300);

        List<Map<String, Object>> operations = jdbc.queryForList(
            """
                select operation_id, login, type, amount, status
                from balance_operations
                """,
            Map.of()
        );

        assertThat(operations).hasSize(1);
        assertThat(operations.getFirst())
            .containsEntry("operation_id", "smoke-balance-operation")
            .containsEntry("login", "bob")
            .containsEntry("type", "WITHDRAW")
            .containsEntry("amount", 200)
            .containsEntry("status", "SUCCESS");
    }

    private void insertAccount(String login, String name, LocalDate birthdate, int balance) {
        jdbc.update(
            """
                insert into accounts(login, name, birthdate, balance)
                values (:login, :name, :birthdate, :balance)
                """,
            new MapSqlParameterSource()
                .addValue("login", login)
                .addValue("name", name)
                .addValue("birthdate", birthdate)
                .addValue("balance", balance)
        );
    }

    private List<Map<String, Object>> loadAccounts() {
        return jdbc.queryForList(
            """
                select login, name, birthdate, balance
                from accounts
                order by login
                """,
            Map.of()
        );
    }
}
