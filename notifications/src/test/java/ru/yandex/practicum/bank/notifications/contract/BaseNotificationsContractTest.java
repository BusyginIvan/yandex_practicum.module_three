package ru.yandex.practicum.bank.notifications.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.notifications.api.controller.NotificationsController;
import ru.yandex.practicum.bank.notifications.config.SecurityConfig;
import ru.yandex.practicum.bank.notifications.config.SecurityTestConfig;
import ru.yandex.practicum.bank.notifications.service.NotificationsService;

import static org.mockito.Mockito.reset;

@AutoConfigureMessageVerifier
@WebMvcTest(NotificationsController.class)
@Import({SecurityConfig.class, SecurityTestConfig.class})
public abstract class BaseNotificationsContractTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    protected NotificationsService notificationsService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        reset(notificationsService);
    }
}
