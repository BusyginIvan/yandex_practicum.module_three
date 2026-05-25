package ru.yandex.practicum.bank.ui.e2e;

import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.yandex.practicum.bank.ui.config.BankUiE2eTestConfig;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.integration.accounts.AccountsClient;
import ru.yandex.practicum.bank.ui.integration.cash.CashClient;
import ru.yandex.practicum.bank.ui.integration.transfers.TransfersClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = BankUiE2eTestConfig.class)
class BankUiE2eTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AccountsClient accountsClient;
    @MockitoBean private CashClient cashClient;
    @MockitoBean private TransfersClient transfersClient;

    @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void beforeEach() {
        reset(
            accountsClient,
            cashClient,
            transfersClient,
            clientRegistrationRepository
        );
        when(accountsClient.getAccounts()).thenReturn(List.of(
            new AccountListItem("alice", "Alice"),
            new AccountListItem("bob", "Bob"),
            new AccountListItem("charlie", null)
        ));
    }

    @Test
    void accountPageShowsProfileAndFiltersOutCurrentAccount() throws Exception {
        when(accountsClient.getMe()).thenReturn(new AccountDetails("alice", "Alice", LocalDate.of(1990, 1, 2), 100));

        String html = mockMvc.perform(get("/account").with(oidcLoginFor("alice")))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(html).contains("Alice");
        assertThat(html).contains("1990-01-02");
        assertThat(html).contains(">100<");
        assertThat(html).contains("<option value=\"bob\">Bob</option>");
        assertThat(html).doesNotContain("<option value=\"alice\">Alice</option>");
    }

    @Test
    void accountUpdateRendersUpdatedProfile() throws Exception {
        when(accountsClient.updateMe("Alice Updated", LocalDate.of(1990, 1, 2)))
            .thenReturn(new AccountDetails("alice", "Alice Updated", LocalDate.of(1990, 1, 2), 100));

        String html = mockMvc.perform(post("/account")
                .with(oidcLoginFor("alice"))
                .with(csrf())
                .param("name", "Alice Updated")
                .param("birthdate", "1990-01-02"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(html).contains("Alice Updated");
        assertThat(html).contains("1990-01-02");
        assertThat(html).contains("<option value=\"bob\">Bob</option>");
        assertThat(html).doesNotContain("<option value=\"alice\">Alice</option>");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operationRequestSpecs")
    void operationRequestUsesCurrentAccountAndShowsMessage(OperationRequestSpec requestSpec) throws Exception {
        when(accountsClient.getMe()).thenReturn(new AccountDetails("alice", "Alice", LocalDate.of(1990, 1, 2), 100));
        requestSpec.stub(clients(accountsClient, cashClient, transfersClient));

        String html = mockMvc.perform(requestSpec.buildRequest().with(oidcLoginFor("alice")).with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(html).contains(requestSpec.expectedMessage());
        assertThat(html).contains("<option value=\"bob\">Bob</option>");
        assertThat(html).doesNotContain("<option value=\"alice\">Alice</option>");
    }

    private static Stream<OperationRequestSpec> operationRequestSpecs() {
        return Stream.of(
            new OperationRequestSpec(
                "cash",
                "Положено 100 руб",
                clients -> when(clients.cashClient().performCashOperation(100, CashOperationType.DEPOSIT))
                    .thenReturn(BalanceOperationStatus.SUCCESS),
                post("/cash")
                    .param("amount", "100")
                    .param("action", CashOperationType.DEPOSIT.name())
            ),
            new OperationRequestSpec(
                "transfer",
                "Перевод на 100 руб пользователю bob выполнен",
                clients -> when(clients.transfersClient().transfer(100, "bob"))
                    .thenReturn(BalanceOperationStatus.SUCCESS),
                post("/transfer")
                    .param("amount", "100")
                    .param("login", "bob")
            )
        );
    }

    private static RequestPostProcessor oidcLoginFor(String login) {
        return oidcLogin().idToken(token -> token.claim("preferred_username", login));
    }

    private ClientMocks clients(
        AccountsClient accountsClient,
        CashClient cashClient,
        TransfersClient transfersClient
    ) {
        return new ClientMocks(accountsClient, cashClient, transfersClient);
    }

    private record OperationRequestSpec(
        String name,
        String expectedMessage,
        Stubber stubber,
        MockHttpServletRequestBuilder requestBuilder
    ) {
        void stub(ClientMocks clients) {
            stubber.stub(clients);
        }

        MockHttpServletRequestBuilder buildRequest() {
            return requestBuilder;
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    private record ClientMocks(
        AccountsClient accountsClient,
        CashClient cashClient,
        TransfersClient transfersClient
    ) { }

    @FunctionalInterface
    private interface Stubber {
        void stub(ClientMocks clients);
    }
}
