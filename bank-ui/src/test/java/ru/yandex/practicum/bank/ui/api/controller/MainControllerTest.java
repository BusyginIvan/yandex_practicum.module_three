package ru.yandex.practicum.bank.ui.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.ui.Model;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.service.MainService;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class MainControllerTest extends AbstractControllerTest {

    @Test
    void indexRedirectsToAccountForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/").with(authenticatedUser()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account"));
    }

    @Test
    void accountRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/account"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void getAccountDelegatesToMainServiceAndReturnsMainView() throws Exception {
        mockMvc.perform(get("/account").with(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(view().name("main"));

        verify(mainService).fillModel(any(Model.class), isNull(), isNull());
    }

    @ParameterizedTest
    @MethodSource("validPostRequests")
    void validPostRequestDelegatesToServiceAndReturnsMainView(PostRequestSpec requestSpec) throws Exception {
        mockMvc.perform(requestSpec.builder()
                .with(authenticatedUser())
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("main"));

        requestSpec.verify(mainService);
    }

    @ParameterizedTest
    @MethodSource("validPostRequests")
    void postRequestRequiresCsrf(PostRequestSpec requestSpec) throws Exception {
        mockMvc.perform(requestSpec.builder()
                .with(authenticatedUser())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isForbidden());

        verifyNoInteractions(mainService);
    }

    @ParameterizedTest
    @MethodSource("invalidPostRequests")
    void invalidPostRequestReturnsBadRequestAndDoesNotCallService(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request
                .with(authenticatedUser())
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(mainService);
    }

    private static Stream<PostRequestSpec> validPostRequests() {
        return Stream.of(
            new PostRequestSpec(
                post("/account")
                    .param("name", "Alice")
                    .param("birthdate", "1990-01-02"),
                service -> verify(service).setNameAndBirthdate(
                    any(Model.class),
                    eq("Alice"),
                    eq(LocalDate.of(1990, 1, 2))
                )
            ),
            new PostRequestSpec(
                post("/cash")
                    .param("amount", "100")
                    .param("action", CashOperationType.DEPOSIT.name()),
                service -> verify(service).performCashOperation(
                    any(Model.class),
                    eq(100),
                    eq(CashOperationType.DEPOSIT)
                )
            ),
            new PostRequestSpec(
                post("/transfer")
                    .param("amount", "100")
                    .param("login", "bob"),
                service -> verify(service).transfer(
                    any(Model.class),
                    eq(100),
                    eq("bob")
                )
            )
        );
    }

    private static Stream<MockHttpServletRequestBuilder> invalidPostRequests() {
        return Stream.of(
            post("/account")
                .param("name", "Alice")
                .param("birthdate", "2999-01-01"),
            post("/account")
                .param("name", "a".repeat(256))
                .param("birthdate", "1990-01-02"),
            post("/cash")
                .param("amount", "0")
                .param("action", CashOperationType.DEPOSIT.name()),
            post("/transfer")
                .param("amount", "100")
                .param("login", "a".repeat(256))
        );
    }

    private static RequestPostProcessor authenticatedUser() {
        return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private record PostRequestSpec(
        MockHttpServletRequestBuilder builder,
        ServiceVerification verification
    ) {
        void verify(MainService service) {
            verification.verify(service);
        }
    }

    @FunctionalInterface
    private interface ServiceVerification {
        void verify(MainService mainService);
    }
}
