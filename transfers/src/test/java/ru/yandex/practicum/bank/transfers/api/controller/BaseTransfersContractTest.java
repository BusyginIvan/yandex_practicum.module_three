package ru.yandex.practicum.bank.transfers.api.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.transfers.config.SecurityConfig;
import ru.yandex.practicum.bank.transfers.config.SecurityTestConfig;
import ru.yandex.practicum.bank.transfers.domain.TransferOperationResult;
import ru.yandex.practicum.bank.transfers.service.TransferService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@AutoConfigureMessageVerifier
@WebMvcTest(TransfersController.class)
@Import({SecurityConfig.class, SecurityTestConfig.class})
public abstract class BaseTransfersContractTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    protected TransferService transferService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        reset(transferService);

        when(transferService.performTransfer(anyInt(), anyString()))
            .thenReturn(TransferOperationResult.SUCCESS);
        when(transferService.performTransfer(100, "poor-bob"))
            .thenReturn(TransferOperationResult.INSUFFICIENT_FUNDS);
        when(transferService.performTransfer(500, "async-bob"))
            .thenReturn(TransferOperationResult.ERROR);
    }
}
