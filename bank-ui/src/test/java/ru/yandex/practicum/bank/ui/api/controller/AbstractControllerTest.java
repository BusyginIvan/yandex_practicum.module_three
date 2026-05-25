package ru.yandex.practicum.bank.ui.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.ui.config.SecurityConfig;
import ru.yandex.practicum.bank.ui.service.MainService;

import static org.mockito.Mockito.reset;

@WebMvcTest
@Import(SecurityConfig.class)
public abstract class AbstractControllerTest {

    @Autowired protected MockMvc mockMvc;

    @MockitoBean protected ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean protected MainService mainService;

    @BeforeEach
    void resetMocks() {
        reset(
            mainService,
            clientRegistrationRepository
        );
    }
}
