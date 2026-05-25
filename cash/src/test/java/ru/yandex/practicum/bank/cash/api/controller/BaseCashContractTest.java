package ru.yandex.practicum.bank.cash.api.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.cash.config.SecurityConfig;
import ru.yandex.practicum.bank.cash.config.SecurityTestConfig;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationResult;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.service.CashService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@AutoConfigureMessageVerifier
@WebMvcTest(CashController.class)
@Import({SecurityConfig.class, SecurityTestConfig.class})
public abstract class BaseCashContractTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    protected CashService cashService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        reset(cashService);

        when(cashService.performCashOperation(anyInt(), any(BalanceOperationType.class)))
            .thenReturn(BalanceOperationResult.SUCCESS);
        when(cashService.performCashOperation(100, BalanceOperationType.WITHDRAW))
            .thenReturn(BalanceOperationResult.INSUFFICIENT_FUNDS);
        when(cashService.performCashOperation(500, BalanceOperationType.DEPOSIT))
            .thenReturn(BalanceOperationResult.ERROR);
    }
}
