package ru.yandex.practicum.bank.accounts.api.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.accounts.config.SecurityConfig;
import ru.yandex.practicum.bank.accounts.config.SecurityTestConfig;
import ru.yandex.practicum.bank.accounts.domain.Account;
import ru.yandex.practicum.bank.accounts.domain.AccountListItem;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;
import ru.yandex.practicum.bank.accounts.exception.OperationIdConflictException;
import ru.yandex.practicum.bank.accounts.service.AccountsService;
import ru.yandex.practicum.bank.accounts.service.BalanceService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@WebMvcTest
@AutoConfigureMessageVerifier
@Import({SecurityConfig.class, SecurityTestConfig.class})
public abstract class BaseAccountsContractTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean protected AccountsService accountsService;
    @MockitoBean protected BalanceService balanceService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        reset(accountsService, balanceService);

        when(accountsService.getCurrentAccount()).thenReturn(
            new Account("alice", "Alice", LocalDate.parse("1990-01-02"), 100)
        );
        when(accountsService.updateCurrentAccount(any(), any())).thenAnswer(invocation -> new Account(
            "alice",
            invocation.getArgument(0, String.class),
            invocation.getArgument(1, LocalDate.class),
            100
        ));
        when(accountsService.getAccounts()).thenReturn(List.of(
            new AccountListItem("alice", "Alice"),
            new AccountListItem("bob", "Bob")
        ));

        when(balanceService.performBalanceOperation(anyString(), anyString(), any(BalanceOperationType.class), anyInt()))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(balanceService.performBalanceOperation(
            "alice",
            "operation-insufficient",
            BalanceOperationType.WITHDRAW,
            100
        )).thenReturn(BalanceOperationResult.INSUFFICIENT_FUNDS);
        when(balanceService.performBalanceOperation(
            "alice",
            "operation-conflict",
            BalanceOperationType.DEPOSIT,
            100
        )).thenThrow(new OperationIdConflictException(
            "operation-conflict",
            "Operation-Id is already used for another request"
        ));
    }
}
