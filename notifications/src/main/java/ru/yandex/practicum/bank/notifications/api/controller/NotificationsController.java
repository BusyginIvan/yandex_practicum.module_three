package ru.yandex.practicum.bank.notifications.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.bank.notifications.api.model.CashNotificationRequest;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    @PostMapping("/cash")
    @PreAuthorize("hasAuthority('notifications:cash')")
    public void sendCashOperationNotification(
        @RequestHeader("Operation-Id") String operationId,
        @RequestBody CashNotificationRequest request
    ) {
    }
}
