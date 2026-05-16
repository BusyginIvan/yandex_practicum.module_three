package ru.yandex.practicum.bank.ui.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import ru.yandex.practicum.bank.ui.domain.AccountDetails;
import ru.yandex.practicum.bank.ui.domain.AccountListItem;
import ru.yandex.practicum.bank.ui.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.ui.domain.CashOperationType;
import ru.yandex.practicum.bank.ui.domain.MessageType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MainServiceTest extends AbstractServiceTest {
    @Autowired
    private MainService mainService;

    @Test
    void setNameAndBirthdateUpdatesProfileAndFillsModel() {
        Model model = mock(Model.class);
        AccountDetails profile = profile("Alice Updated", LocalDate.of(1990, 1, 2), 100);
        when(accountsClient.updateMe("Alice Updated", LocalDate.of(1990, 1, 2))).thenReturn(profile);
        stubOtherAccounts();

        mainService.setNameAndBirthdate(model, "Alice Updated", LocalDate.of(1990, 1, 2));

        verify(accountsClient).updateMe("Alice Updated", LocalDate.of(1990, 1, 2));
        assertModelFilled(model, profile, null, null);
    }

    @ParameterizedTest
    @MethodSource("cashOperationCases")
    void performCashOperationFillsModel(
        BalanceOperationStatus status,
        CashOperationType type,
        String expectedMessage,
        MessageType expectedMessageType
    ) {
        Model model = mock(Model.class);
        AccountDetails profile = profile("Alice", LocalDate.of(1990, 1, 2), 100);
        when(cashClient.performCashOperation(100, type)).thenReturn(status);
        when(accountsClient.getMe()).thenReturn(profile);
        stubOtherAccounts();

        mainService.performCashOperation(model, 100, type);

        verify(cashClient).performCashOperation(100, type);
        assertModelFilled(model, profile, expectedMessage, expectedMessageType);
    }

    @ParameterizedTest
    @MethodSource("transferCases")
    void transferFillsModel(
        BalanceOperationStatus status,
        String login,
        String expectedMessage,
        MessageType expectedMessageType
    ) {
        Model model = mock(Model.class);
        AccountDetails profile = profile("Alice", LocalDate.of(1990, 1, 2), 100);
        when(transfersClient.transfer(100, login)).thenReturn(status);
        when(accountsClient.getMe()).thenReturn(profile);
        stubOtherAccounts();

        mainService.transfer(model, 100, login);

        verify(transfersClient).transfer(100, login);
        assertModelFilled(model, profile, expectedMessage, expectedMessageType);
    }

    @Test
    void fillModelLoadsProfileAndOtherAccounts() {
        Model model = mock(Model.class);
        AccountDetails profile = profile(null, null, 250);
        when(accountsClient.getMe()).thenReturn(profile);
        stubOtherAccounts();

        mainService.fillModel(model, "Saved", MessageType.SUCCESS);

        verify(accountsClient).getMe();
        assertModelFilled(model, profile, "Saved", MessageType.SUCCESS);
    }

    private void stubOtherAccounts() {
        when(currentAccountService.getCurrentLogin()).thenReturn("alice");
        when(accountsClient.getAccounts()).thenReturn(List.of(
            new AccountListItem("alice", "Alice"),
            new AccountListItem("bob", "Bob"),
            new AccountListItem("charlie", null)
        ));
    }

    private void assertModelFilled(Model model, AccountDetails profile, String message, MessageType messageType) {
        verify(model).addAttribute("name", profile.nameOrEmpty());
        verify(model).addAttribute("birthdate", profile.birthdateAsString());
        verify(model).addAttribute("balance", profile.balance());
        verify(model).addAttribute("accounts", List.of(new AccountListItem("bob", "Bob")));
        verify(model).addAttribute("message", message);
        verify(model).addAttribute("messageType", messageType);
    }

    private AccountDetails profile(String name, LocalDate birthdate, int balance) {
        return new AccountDetails("alice", name, birthdate, balance);
    }

    private static Stream<Arguments> cashOperationCases() {
        return Stream.of(
            Arguments.of(BalanceOperationStatus.SUCCESS, CashOperationType.DEPOSIT,
                "Положено 100 руб", MessageType.SUCCESS),
            Arguments.of(BalanceOperationStatus.SUCCESS, CashOperationType.WITHDRAW,
                "Снято 100 руб", MessageType.SUCCESS),
            Arguments.of(BalanceOperationStatus.INSUFFICIENT_FUNDS, CashOperationType.WITHDRAW,
                "Недостаточно средств на счету", MessageType.ERROR),
            Arguments.of(BalanceOperationStatus.PROCESSING, CashOperationType.DEPOSIT,
                "Операция в обработке", MessageType.PENDING),
            Arguments.of(BalanceOperationStatus.ERROR, CashOperationType.DEPOSIT,
                "Что-то пошло не так", MessageType.ERROR)
        );
    }

    private static Stream<Arguments> transferCases() {
        return Stream.of(
            Arguments.of(BalanceOperationStatus.SUCCESS, "bob",
                "Перевод на 100 руб пользователю bob выполнен", MessageType.SUCCESS),
            Arguments.of(BalanceOperationStatus.INSUFFICIENT_FUNDS, "bob",
                "Недостаточно средств на счету", MessageType.ERROR),
            Arguments.of(BalanceOperationStatus.PROCESSING, "bob",
                "Перевод в обработке", MessageType.PENDING),
            Arguments.of(BalanceOperationStatus.ERROR, "bob",
                "Что-то пошло не так", MessageType.ERROR)
        );
    }
}
